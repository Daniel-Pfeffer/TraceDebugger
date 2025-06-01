@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package at.jku.ssw

import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.Name
import com.sun.tools.javac.util.Names

internal class AstGenerator(
    private val names: Names,
    private val treeMaker: TreeMaker,
) {
    private val traceRecoder = names.fromString("$$\$TraceRecorder")

    fun generateTraceMethodEntry(
        className: Name,
        methodName: Name,
    ): JCTree.JCExpressionStatement {
        val args = List.of<JCTree.JCExpression>(
            treeMaker.Literal(className.toString()),
            treeMaker.Literal(methodName.toString()),
        )

        return treeMaker.Exec(
            treeMaker.Apply(
                List.nil(),
                treeMaker.Select(
                    treeMaker.Ident(traceRecoder),
                    names.fromString("enterMethod"),
                ),
                args
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

    fun generateTraceRecordReturn(toReturn: JCTree.JCExpression): JCTree.JCExpression {
        return treeMaker.Apply(
            List.nil(),
            treeMaker.Select(
                treeMaker.Ident(traceRecoder),
                names.fromString("returnValue")
            ),
            List.of(toReturn)
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

    fun generateTraceRecordAssignLocal(
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

    fun generateTraceRecordAssignField(
        obj: JCTree.JCExpression,
        fieldName: Name,
        value: JCTree.JCExpression,
    ): JCTree.JCExpression {
        return treeMaker.Apply(
            List.nil(),
            treeMaker.Select(
                treeMaker.Ident(traceRecoder),
                names.fromString("setField")
            ),
            List.of(
                obj,
                treeMaker.Literal(fieldName.toString()),
                value
            )
        )
    }

    fun generateTraceRecordAssignStatic(
        className: Name,
        fieldName: Name,
        value: JCTree.JCExpression,
    ): JCTree.JCExpression {
        return treeMaker.Apply(
            List.nil(),
            treeMaker.Select(
                treeMaker.Ident(traceRecoder),
                names.fromString("setStaticVariable")
            ),
            List.of(
                treeMaker.Literal(className.toString()),
                treeMaker.Literal(fieldName.toString()),
                value
            )
        )
    }

    fun generateTraceRecordUnaryLocal(
        toAssign: Name,
        value: JCTree.JCExpression,
    ): JCTree.JCExpression {
        return treeMaker.Apply(
            List.nil(),
            treeMaker.Select(
                treeMaker.Ident(traceRecoder),
                names.fromString("unaryLocal")
            ),
            List.of(
                treeMaker.Literal(toAssign.toString()),
                value,
                treeMaker.Ident(toAssign)
            )
        )
    }

    fun generateTraceRecordUnaryStatic(
        className: Name,
        toAssign: Name,
        value: JCTree.JCExpression,
    ): JCTree.JCExpression {
        return treeMaker.Apply(
            List.nil(),
            treeMaker.Select(
                treeMaker.Ident(traceRecoder),
                names.fromString("unaryStatic")
            ),
            List.of(
                treeMaker.Literal(className.toString()),
                treeMaker.Literal(toAssign.toString()),
                value,
                treeMaker.Ident(toAssign)
            )
        )
    }

    fun generateTraceRecordUnaryField(
        obj: JCTree.JCExpression,
        toAssign: Name,
        value: JCTree.JCExpression,
    ): JCTree.JCExpression {
        return treeMaker.Apply(
            List.nil(),
            treeMaker.Select(
                treeMaker.Ident(traceRecoder),
                names.fromString("unaryField")
            ),
            List.of(
                obj,
                treeMaker.Literal(toAssign.toString()),
                value,
                treeMaker.Ident(toAssign)
            )
        )
    }


    fun generateTraceRecordCreateNewObject(
        value: JCTree.JCExpression
    ): JCTree.JCExpression {
        return treeMaker.Apply(
            List.nil(),
            treeMaker.Select(
                treeMaker.Ident(traceRecoder),
                names.fromString("recordNewObjectCreation")
            ),
            List.of(value)
        )
    }

}