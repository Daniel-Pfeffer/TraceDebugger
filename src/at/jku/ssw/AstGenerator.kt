@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package at.jku.ssw

import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.util.Names
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name

internal class AstGenerator(
    private val names: Names,
    private val treeMaker: TreeMaker,
) {
    private val traceRecoder = names.fromString("$$\$TraceRecorder")

    fun generateVariableDefinition(
        name: String,
        init: JCTree.JCExpression
    ): JCTree.JCVariableDecl {
        return treeMaker.VarDef(
            treeMaker.Modifiers(Flags.FINAL.toLong()),
            names.fromString(name),
            null,
            init,
            true
        )
    }

    fun generateTraceMethodEntry(
        className: Name,
        methodName: Name,
    ): JCTree.JCExpressionStatement {
        return treeMaker.Exec(
            treeMaker.Apply(
                List.nil(),
                treeMaker.Select(
                    treeMaker.Ident(traceRecoder),
                    names.fromString("enterMethod")
                ),
                List.of(
                    treeMaker.Literal(className.toString()),
                    treeMaker.Literal(methodName.toString())
                )
            )
        )
    }

    fun generateTraceReturnValue(name: Name): JCTree.JCExpressionStatement {
        return treeMaker.Exec(
            treeMaker.Apply(
                List.nil(),
                treeMaker.Select(
                    treeMaker.Ident(traceRecoder),
                    names.fromString("returnValue")
                ),
                List.of(treeMaker.Ident(name))
            )
        )
    }

    fun generateMethodExit(): JCTree.JCExpressionStatement {
        return treeMaker.Exec(
            treeMaker.Apply(
                List.nil(),
                treeMaker.Select(
                    treeMaker.Ident(traceRecoder),
                    names.fromString("exitMethod")
                ),
                List.nil()
            )
        )
    }

    fun generateTraceRecorderImport(): JCTree.JCImport {
        return treeMaker.Import(
            treeMaker.Select(
                treeMaker.Ident(names.fromString("$\$jwdebug")),
                traceRecoder
            ),
            false
        )
    }

    fun generateTraceRecordEnd(): JCTree.JCExpressionStatement {
        return treeMaker.Exec(
            treeMaker.Apply(
                List.nil(),
                treeMaker.Select(
                    treeMaker.Ident(traceRecoder),
                    names.fromString("verifyFinished")
                ),
                List.nil()
            )
        )
    }

    fun generateTraceRecordCondition(condition: JCTree.JCExpression): JCTree.JCExpression {
        return treeMaker.Apply(
            List.nil(),
            treeMaker.Select(
                treeMaker.Ident(traceRecoder),
                names.fromString("recordCondition")
            ),
            List.of(condition, treeMaker.Literal(condition.toString()))
        )
    }

    fun generateTraceRecordAssign(
        toAssign: Name,
        value: JCTree.JCExpression,
    ): JCTree.JCExpression {
        return treeMaker.Apply(
            List.nil(),
            treeMaker.Select(
                treeMaker.Ident(traceRecoder),
                names.fromString("setLocalVariable")
            ),
            List.of(
                treeMaker.Literal(toAssign.toString()),
                value
            )
        )

    }

}