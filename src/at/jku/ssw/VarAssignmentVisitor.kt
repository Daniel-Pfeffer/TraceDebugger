@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package at.jku.ssw

import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.tree.JCTree.*
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeScanner
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.Context
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.ListBuffer
import com.sun.tools.javac.util.Name
import com.sun.tools.javac.util.Names
import kotlin.collections.set
import kotlin.collections.List as KList

internal class VarAssignmentVisitor private constructor(
    private val treeMaker: TreeMaker,
    private val names: Names,
) : TreeTranslator() {

    companion object {
        fun generate(tree: JCCompilationUnit, context: Context) {
            val treeMaker = TreeMaker.instance(context)
            val names = Names.instance(context)
            val visitor = VarAssignmentVisitor(treeMaker, names)
            tree.accept(visitor)
        }

        private const val ANON_PREFIX = "$\$anon"
    }

    override fun visitMethodDef(methodDecl: JCMethodDecl) {
        val enterStatement = generateSysOutCall("Enter method ${methodDecl.name}")
        val exitStatement = generateSysOutCall("Exit method ${methodDecl.name}")

        // add at beginning
        if (methodDecl.body != null) {
            methodDecl.body = treeMaker.Block(
                0,
                methodDecl.body.stats.prepend(enterStatement).append(exitStatement)
            )
        }

        super.visitMethodDef(methodDecl)
    }

    override fun visitVarDef(tree: JCVariableDecl) {
        println("Visiting var decl (${tree.pos}): $tree")
        super.visitVarDef(tree)
    }

    override fun visitLabelled(tree: JCLabeledStatement) {
        super.visitLabelled(tree)
    }

    override fun visitBlock(tree: JCBlock) {
        println("Visiting new block")
        if (tree.flags == Flags.SYNTHETIC.toLong()) {
            println("Found synthetic block, doing normal visit")
            return super.visitBlock(tree)
        }
        val newStats = ListBuffer<JCStatement>()
        val collector = AssignmentCollector()
        for (stat in tree.stats) {
            collector.scan(stat)

            if (collector.modified) {
                newStats.appendList(collector.newStatements)
            } else {
                newStats.append(stat)
            }
            collector.reset()
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
    }

    fun generateSysOutCall(param: String): JCExpressionStatement {
        val systemOut = treeMaker.Select(
            treeMaker.Ident(names.fromString("System")),
            names.fromString("out")
        )

        val printlnCall = treeMaker.Apply(
            List.nil(),
            treeMaker.Select(systemOut, names.fromString("println")),
            List.of(treeMaker.Literal(param))
        )
        return treeMaker.Exec(printlnCall)
    }


    fun generateSysOutCall(variable: JCExpression, value: JCVariableDecl): JCExpressionStatement? {
        val systemOut = treeMaker.Select(
            treeMaker.Ident(names.fromString("System")),
            names.fromString("out")
        )

        val literal = when (variable) {
            is JCIdent -> treeMaker.Literal("Assigning " + variable.name + " to value: ")
            // if variable.selected is JCIdent, then it is a field access pointing to local variable with name inner in script
            is JCFieldAccess -> treeMaker.Literal("Assigning ${variable.name} in object ${variable.selected} to value: ")
            else -> treeMaker.Literal("Assigning to value: ")
        }

        val message = treeMaker.Binary(
            Tag.PLUS,
            literal,
            treeMaker.Ident(value.name)
        )

        val printlnCall = treeMaker.Apply(
            List.nil(),
            treeMaker.Select(systemOut, names.fromString("println")),
            List.of(message)
        )
        return treeMaker.Exec(printlnCall)
    }


    private inner class AssignmentCollector : TreeScanner() {
        var modified = false
        var newStatements: List<JCStatement> = List.nil()

        val mapping = mutableMapOf<Name, JCExpression>()

        fun reset() {
            modified = false
            newStatements = List.nil()
        }

        override fun visitVarDef(tree: JCVariableDecl) {
            mapping[tree.name] = tree.vartype;
            super.visitVarDef(tree)
        }

        override fun visitExec(tree: JCExpressionStatement) {
            if (tree.expr is JCAssign) {
                val assign = tree.expr as JCAssign

                val variableName = variableName(assign.variable)

                if (variableName.startsWith(ANON_PREFIX)) {
                    println("Skipping assignment to anonymous variable: $variableName")
                    super.visitExec(tree)
                    return
                }

                val anonName = names.fromString(generateAnonName(variableName))


                /*val varType = mapping[variableName]
                    ?: error("Variable type not found for $variableName")*/

                val anonVariableDefinition = treeMaker.VarDef(
                    treeMaker.Modifiers(Flags.FINAL.toLong()),
                    anonName,
                    null,
                    assign.expression,
                    true
                )

                val beforeAssign = generateSysOutCall(assign.variable, anonVariableDefinition)

                val copyFromAnonToActual = treeMaker.Exec(
                    treeMaker.at(assign.pos).Assign(
                        assign.variable,
                        treeMaker.Ident(anonName)
                    )
                )

                newStatements = List.of<JCStatement>(anonVariableDefinition, beforeAssign, copyFromAnonToActual)
                modified = true
            } else {
                super.visitExec(tree)
            }
        }

        private fun variableName(tree: JCExpression): Name {
            return when (tree) {
                is JCIdent -> tree.name
                is JCFieldAccess -> tree.name
                else -> error("Unknown expression type")
            }
        }

        private fun generateAnonName(original: Name): String {
            return ANON_PREFIX + original
        }
    }
}
