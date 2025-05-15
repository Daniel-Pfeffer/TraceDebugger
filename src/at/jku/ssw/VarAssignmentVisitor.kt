@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package at.jku.ssw

import com.sun.tools.javac.tree.JCTree.*
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.Context
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.Names

internal class VarAssignmentVisitor private constructor(
    private val treeMaker: TreeMaker,
    private val names: Names,
) : TreeTranslator() {

    private var currentBlock: JCBlock? = null
    private var stmtCount: Int = 0

    companion object {
        fun generate(tree: JCCompilationUnit, context: Context) {
            val treeMaker = TreeMaker.instance(context)
            val names = Names.instance(context)
            val visitor = VarAssignmentVisitor(treeMaker, names)
            tree.accept(visitor)
        }
    }

    override fun visitMethodDef(methodDecl: JCMethodDecl) {
        val soutStatement = generateSysOutCall("Visiting method: " + methodDecl.name)

        // add at beginning
        if (methodDecl.body != null) {
            methodDecl.body = treeMaker.Block(
                0,
                methodDecl.body.stats.prepend(soutStatement)
            )
        }

        super.visitMethodDef(methodDecl)
    }

    override fun visitAssign(tree: JCAssign) {

        println("Visiting assignment: " + tree.lhs + " = " + tree.rhs)
        val soutStatement = generateSysOutCall("Assigning ${tree.rhs} to: " + tree.lhs)

        treeMaker.at(tree.pos).Block(
            0,
            List.of<JCStatement>(soutStatement, treeMaker.Exec(tree))
        )

        currentBlock?.stats = currentBlock?.stats?.prepend(soutStatement)

        super.visitAssign(tree)
        stmtCount++
    }

    override fun visitLabelled(tree: JCLabeledStatement) {
        stmtCount++
        super.visitLabelled(tree)
    }

    override fun visitBlock(tree: JCBlock?) {
        println("Visiting new block")
        currentBlock = tree
        super.visitBlock(tree)
        println("Exit block")
        currentBlock = null
    }

    override fun visitExec(tree: JCExpressionStatement) {
        println("Visiting expression: " + tree.expr)
        val block = treeMaker.at(tree.pos).Block(
            0,
            List.of<JCStatement>(generateSysOutCall("Visiting expression: " + tree.expr), tree)
        )

        super.visitExec(tree)
    }

    private fun generateSysOutCall(param: String): JCExpressionStatement {
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
}