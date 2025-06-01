@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package at.jku.ssw

import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.code.TypeTag
import com.sun.tools.javac.tree.JCTree.*
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.*
import com.sun.tools.javac.util.List
import java.util.*
import kotlin.collections.List as KList

internal class VarAssignmentVisitor private constructor(
    private val treeMaker: TreeMaker,
    private val names: Names,
) : TreeTranslator() {

    private val generator = AstGenerator(names, treeMaker)

    private val classStack: Stack<Name> = Stack()

    private val localVariableStack: Stack<MutableList<Name>> = Stack()

    companion object {
        fun generate(tree: JCCompilationUnit, context: Context) {
            val treeMaker = TreeMaker.instance(context)
            val names = Names.instance(context)
            val visitor = VarAssignmentVisitor(treeMaker, names)
            tree.accept(visitor)
        }

        private const val ANON_PREFIX = "$\$anon"
    }

    // TODO: line numbers, method signatures
    override fun visitMethodDef(methodDecl: JCMethodDecl) {
        localVariableStack.push(mutableListOf())
        val enterStatement = generator.generateTraceMethodEntry(
            classStack.peek(),
            methodDecl.name,
        )

        val newStats = ListBuffer<JCStatement>()
        newStats.append(enterStatement)

        methodDecl.params.forEach {
            newStats.append(
                treeMaker.Exec(generator.generateTraceRecordAssignLocal(it.name, treeMaker.Ident(it.name)))
            )
        }

        if (methodDecl.body != null) {
            val statements = methodDecl.body.stats ?: List.nil()
            val lastStatement = statements.last()

            newStats.appendList(statements)

            if (lastStatement !is JCReturn) {
                // TODO: somehow fix this issues, that if every branch has a return, we still generate an exit statement, which is not needed
                val exitStatement = generator.generateMethodExit()
                newStats.append(exitStatement)
            }
        }
        if (methodDecl.name == names.main) {
            val tryCatch = treeMaker.Try(
                treeMaker.Block(0, newStats.toList()),
                List.nil(),
                treeMaker.Block(
                    0,
                    List.of(
                        generator.generateTraceRecordEnd()
                    )
                )
            )

            methodDecl.body = treeMaker.Block(
                methodDecl.body.flags,
                List.of(tryCatch)
            )
        } else {
            methodDecl.body = treeMaker.Block(
                methodDecl.body.flags,
                newStats.toList()
            )
        }



        super.visitMethodDef(methodDecl)
        localVariableStack.pop()
    }

    override fun visitTopLevel(tree: JCCompilationUnit) {
        val newImport = generator.generateTraceRecorderImport()
        tree.defs = tree.defs.prepend(newImport)
        super.visitTopLevel(tree)
    }

    override fun visitVarDef(tree: JCVariableDecl) {
        if (Flags.asFlagSet(tree.mods.flags).contains(Flags.Flag.STATIC)) {
            println("Found static variable declaration: ${tree.name}")
        }
        super.visitVarDef(tree)
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
                    val traceCondition = generator.generateTraceRecordCondition(stat.cond)

                    val newCondition = treeMaker.If(
                        traceCondition,
                        stat.thenpart,
                        stat.elsepart
                    )

                    newStats.append(newCondition)
                }

                is JCForLoop -> {
                    val init = stat.init

                    val newInit = ListBuffer<JCStatement>()

                    init.forEach {
                        when (it) {
                            is JCVariableDecl -> {
                                newInit.append(handleVariableDeclaration(it))
                            }

                            is JCExpressionStatement -> {
                                newStats.append(it)
                            }

                            else -> {
                                println("Unknown init type in for loop: $it")
                            }
                        }
                    }

                    val newCondition = stat.cond?.let { cond ->
                        generator.generateTraceRecordCondition(cond)
                    } ?: generator.generateTraceRecordCondition(treeMaker.Literal(true))

                    val newStep = stat.step.map { step ->
                        treeMaker.Exec(handleExpression(step.expr))
                    }

                    val forLoop = treeMaker.ForLoop(
                        newInit.toList(),
                        newCondition,
                        newStep,
                        stat.body
                    )

                    newStats.append(forLoop)
                }

                is JCVariableDecl -> {
                    newStats.append(handleVariableDeclaration(stat))
                }

                is JCExpressionStatement -> {
                    newStats.append(
                        treeMaker.Exec(
                            handleExpression(stat.expr)
                        )
                    )
                }

                is JCReturn -> {
                    val beforeReturn = generator.generateMethodExit()
                    val returnActual = treeMaker.Return(
                        generator.generateTraceRecordReturn(
                            handleExpression(stat.expr)
                        )
                    )

                    newStats.appendList(
                        List.of<JCStatement>(
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

    private fun handleExpression(expr: JCExpression): JCExpression {
        // TODO: handling arrays, handle exception throws
        return when (expr) {
            is JCAssign -> handleAssign(expr)

            is JCBinary -> {
                return treeMaker.Binary(
                    expr.tag,
                    handleExpression(expr.lhs),
                    handleExpression(expr.rhs)
                )
            }

            is JCAssignOp -> handleAssignOp(expr)

            is JCUnary -> handleUnary(expr)

            else -> {
                // If the expression is not an assignment, we just append it
                expr
            }
        }
    }

    private fun handleVariableDeclaration(varDecl: JCVariableDecl): JCVariableDecl {
        if (varDecl.init == null) {
            return varDecl
        }
        localVariableStack.peek().add(varDecl.name)
        val isVarTypeOfDeclObject = varDecl.vartype is JCIdent

        val fakeAssign = handleAssign(
            treeMaker.Assign(
                treeMaker.Ident(varDecl.name),
                varDecl.init
            )
        )

        val init = if (isVarTypeOfDeclObject) {
            // need to type cast the generated record, as it returns an "object", instead of T, could probably be done generically
            treeMaker.TypeCast(varDecl.vartype, fakeAssign.rhs)
        } else {
            fakeAssign.rhs
        }
        return treeMaker.VarDef(
            varDecl.mods,
            varDecl.name,
            varDecl.vartype,
            init,
        )
    }

    private fun handleAssignOp(assignOp: JCAssignOp): JCAssign {
        return handleAssign(
            treeMaker.Assign(
                assignOp.lhs,
                treeMaker.Binary(assignOp.tag.noAssignOp(), assignOp.lhs, assignOp.rhs)
            )
        )
    }

    private fun handleUnary(expr: JCUnary): JCExpression {
        val variableName = variableName(expr.arg)

        val newUnary = if (variableName.startsWith(ANON_PREFIX)) {
            println("Skipping unary operation on anonymous variable: $variableName")
            expr
        } else {
            val currentLocalVariables = localVariableStack.peek()
            if (currentLocalVariables.contains(variableName)) {
                generator.generateTraceRecordUnaryLocal(variableName, expr)
            } else {
                val currentFieldsOrStatic = classRegistrations.getValue(classStack.peek()).variables

                val existingField = currentFieldsOrStatic.firstOrNull { it.name == variableName }
                if (existingField != null && existingField.flags.contains(Flags.Flag.STATIC)) {
                    generator.generateTraceRecordUnaryStatic(
                        classStack.peek(),
                        variableName,
                        expr
                    )
                } else {
                    generator.generateTraceRecordUnaryField(
                        treeMaker.Ident(names._this),
                        variableName,
                        expr
                    )
                }
            }
        }

        return newUnary
    }

    private fun handleAssign(assign: JCAssign): JCAssign {
        val variableName = variableName(assign.variable)

        if (variableName.startsWith(ANON_PREFIX)) {
            println("Skipping assignment to anonymous variable: $variableName")
            return assign
        } else {
            val lhs = assign.lhs
            val rhs = handleExpression(assign.rhs)


            val testObjectCreationRhs = if (rhs is JCNewClass) {
                generator.generateTraceRecordCreateNewObject(rhs)
            } else if (rhs is JCLiteral && rhs.typetag == TypeTag.CLASS) {
                generator.generateTraceRecordCreateNewObject(rhs)
            } else {
                rhs
            }

            val newRhs = if (lhs is JCFieldAccess) {
                // generateTraceRecordAssignField
                if (lhs.selected is JCIdent && (lhs.selected as JCIdent).name == names._this && rhs is JCLiteral && rhs.typetag == TypeTag.BOT /*null*/) {
                    val currentFieldsOrStatic = classRegistrations.getValue(classStack.peek()).variables
                    val existingField = currentFieldsOrStatic.first { it.name == lhs.name }
                    generator.generateTraceRecordAssignField(
                        treeMaker.Ident(names._this),
                        lhs.name,
                        treeMaker.TypeCast(existingField.varType, testObjectCreationRhs)
                    )
                } else {
                    generator.generateTraceRecordAssignField(
                        lhs.selected,
                        lhs.name,
                        testObjectCreationRhs
                    )
                }
            } else if (lhs is JCIdent) {
                val currentLocalVariables = localVariableStack.peek()
                if (currentLocalVariables.contains(lhs.name)) {
                    generator.generateTraceRecordAssignLocal(
                        variableName,
                        testObjectCreationRhs
                    )
                } else {
                    val currentFieldsOrStatic = classRegistrations.getValue(classStack.peek()).variables

                    val existingField = currentFieldsOrStatic.first { it.name == lhs.name }
                    if (existingField.flags.contains(Flags.Flag.STATIC)) {
                        generator.generateTraceRecordAssignStatic(
                            classStack.peek(),
                            lhs.name,
                            testObjectCreationRhs
                        )
                    } else {
                        generator.generateTraceRecordAssignField(
                            treeMaker.Ident(names._this),
                            lhs.name,
                            testObjectCreationRhs
                        )
                    }
                }

            } else {
                error("Unknown assignment target: $lhs")
            }
            return treeMaker.at(assign.pos).Assign(
                assign.variable,
                newRhs
            )

        }
    }

    override fun visitClassDef(tree: JCClassDecl) {
        val className = tree.name
        // TODO: handle static variable definitions
        val varDecls = tree.defs.filterIsInstance<JCVariableDecl>().map {
            val varType = it.vartype
            val name = it.name
            val modifier = Flags.asFlagSet(it.mods.flags)
            val variable = Variable(name, varType, modifier)
            variable
        }
        registerClass(className, varDecls, emptyList())
        super.visitClassDef(tree)
        classStack.pop()
    }

    val classRegistrations = mutableMapOf<Name, ClassRegistration>()

    data class Variable(
        val name: Name,
        val varType: JCExpression,
        val flags: EnumSet<Flags.Flag>
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

    private fun variableName(tree: JCExpression): Name {
        return when (tree) {
            is JCIdent -> tree.name
            is JCFieldAccess -> tree.name
            else -> error("Unknown expression type")
        }
    }
}
