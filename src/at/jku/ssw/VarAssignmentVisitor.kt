@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package at.jku.ssw

import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.tree.JCTree.*
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.Context
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.ListBuffer
import com.sun.tools.javac.util.Name
import com.sun.tools.javac.util.Names
import java.util.Stack
import kotlin.collections.set
import kotlin.collections.List as KList

internal class VarAssignmentVisitor private constructor(
    private val treeMaker: TreeMaker,
    private val names: Names,
) : TreeTranslator() {

    private val generator = AstGenerator(names, treeMaker)

    private val classStack: Stack<Name> = Stack()

    companion object {
        fun generate(tree: JCCompilationUnit, context: Context) {
            val treeMaker = TreeMaker.instance(context)
            val names = Names.instance(context)
            val visitor = VarAssignmentVisitor(treeMaker, names)
            tree.accept(visitor)
        }

        private const val ANON_PREFIX = "$\$anon"

        private var anonCounter = 0 // incredibly ugly, probably should do variable reusing for that
    }

    override fun visitMethodDef(methodDecl: JCMethodDecl) {
        val enterStatement = generator.generateTraceMethodEntry(classStack.peek(), methodDecl.name)

        val newStats = ListBuffer<JCStatement>()
        newStats.append(enterStatement)

        if (methodDecl.body != null) {
            val statements = methodDecl.body.stats ?: List.nil()
            val shouldAppend = statements.last() !is JCReturn

            newStats.appendList(statements)

            if (shouldAppend) {
                val exitStatement = generator.generateMethodExit()
                newStats.append(exitStatement)
            }
        }
        if (methodDecl.name == names.main) {
            newStats.append(generator.generateTraceRecordEnd())
        }

        methodDecl.body = treeMaker.Block(
            methodDecl.body.flags,
            newStats.toList()
        )

        super.visitMethodDef(methodDecl)
    }

    override fun visitTopLevel(tree: JCCompilationUnit) {
        val newImport = generator.generateTraceRecorderImport()
        tree.defs = tree.defs.prepend(newImport)
        super.visitTopLevel(tree)
    }

    override fun visitBlock(tree: JCBlock) {
        println("Visiting new block")
        if (tree.flags == Flags.SYNTHETIC.toLong()) {
            println("Found synthetic block, doing normal visit")
            return super.visitBlock(tree)
        }
        val newStats = ListBuffer<JCStatement>()
        for (stat in tree.stats) {
            when (stat) {
                is JCIf -> {
                    // TODO: add extract condition, else let it be
                    val condition = stat.cond

                    val traceCondition = generator.generateTraceRecordCondition(condition)

                    val newCondition = treeMaker.If(
                        traceCondition,
                        stat.thenpart,
                        stat.elsepart
                    )

                    newStats.append(newCondition)
                }

                is JCForLoop -> {
                    // TODO: add extract loop stuff, else let it be
                    /*                    val init = stat.init
                                        val condition = stat.cond
                                        val step = stat.step
                                        val loopInitName = names.fromString(generateAnonName("loopInit"))
                                        val loopConditionName = names.fromString(generateAnonName("loopCondition"))
                                        val loopInitDecl =
                                            generateVariableDefinition(loopInitName, init.firstOrNull() ?: treeMaker.Literal(""))*/
                    println("Found for loop")
                    stat.step

                    newStats.append(stat) // TODO: handle for loop
                }

                is JCExpressionStatement -> {
                    when (stat.expr) {
                        is JCAssign -> {
                            val assign = stat.expr as JCAssign

                            val variableName = variableName(assign.variable)

                            if (variableName.startsWith(ANON_PREFIX)) {
                                println("Skipping assignment to anonymous variable: $variableName")
                                newStats.append(stat)
                            } else {
                                newStats.append(
                                    treeMaker.Exec(
                                        treeMaker.at(assign.pos).Assign(
                                            assign.variable,
                                            generator.generateTraceRecordAssign(
                                                variableName,
                                                assign.rhs
                                            )
                                        )
                                    )
                                )


                            }
                        }

                        else -> {
                            // If the expression is not an assignment, we just append it
                            newStats.append(stat)
                        }
                    }
                }

                is JCReturn -> {
                    val anonName = generateAnonName(names.fromString("return"))

                    val anonVariableDefinition = generator.generateVariableDefinition(
                        anonName,
                        stat.expr
                    )

                    val beforeReturn = generator.generateMethodExit()

                    val returnActual = treeMaker.Return(treeMaker.Ident(anonVariableDefinition.name))

                    newStats.appendList(
                        List.of<JCStatement>(
                            anonVariableDefinition,
                            generator.generateTraceReturnValue(anonVariableDefinition.name),
                            beforeReturn,
                            returnActual
                        )
                    )
                }

                is JCSwitch -> {
                    println("Found switch statement")
                }

                else -> {
                    newStats.append(stat)
                }
            }
        }
        tree.stats = newStats.toList()
        super.visitBlock(tree)
    }

    override fun visitClassDef(tree: JCClassDecl) {
        val className = tree.name
        val varDecls = tree.defs.filterIsInstance<JCVariableDecl>().map {
            val varType = it.vartype
            val name = it.name
            val variable = Variable(name, varType)
            variable
        }
        registerClass(className, varDecls, emptyList())
        super.visitClassDef(tree)
        classStack.pop()
    }

    val classRegistrations = mutableMapOf<Name, ClassRegistration>()

    data class Variable(
        val name: Name,
        val varType: JCExpression
    )

    data class Method(
        val name: Name,
        val returnType: JCExpression,
        // TODO
    )

    data class ClassRegistration(
        val name: Name,
        val variables: KList<Variable>,
        val method: KList<Method>
    )

    private fun registerClass(
        identifier: Name,
        decls: KList<Variable>,
        methods: KList<Method>
    ) {
        if (classRegistrations.containsKey(identifier)) {
            println("Class $identifier already registered, skipping")
            return
        }

        val classRegistration = ClassRegistration(
            identifier,
            decls,
            methods
        )
        println("Registering class $identifier with variables: $decls and methods: $methods")

        classRegistrations[identifier] = classRegistration
        classStack.push(identifier)
    }

    fun generateSysOutCall(param: String, value: JCVariableDecl): JCExpressionStatement {
        val systemOut = treeMaker.Select(
            treeMaker.Ident(names.fromString("System")),
            names.fromString("out")
        )

        val printlnCall = treeMaker.Apply(
            List.nil(),
            treeMaker.Select(systemOut, names.fromString("println")),
            List.of(treeMaker.Binary(Tag.PLUS, treeMaker.Literal(param), treeMaker.Ident(value.name)))
        )
        return treeMaker.Exec(printlnCall)
    }

    private fun generateAnonName(original: Name): String {
        return ANON_PREFIX + original + anonCounter++
    }

    private fun generateAnonName(original: String): String {
        return ANON_PREFIX + original + anonCounter++
    }

    private fun variableName(tree: JCExpression): Name {
        return when (tree) {
            is JCIdent -> tree.name
            is JCFieldAccess -> tree.name
            else -> error("Unknown expression type")
        }
    }
}
