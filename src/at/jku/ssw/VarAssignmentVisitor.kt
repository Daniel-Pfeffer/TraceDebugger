@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package at.jku.ssw

import com.sun.tools.javac.tree.JCTree
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