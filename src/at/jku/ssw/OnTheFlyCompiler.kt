@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package at.jku.ssw

import com.sun.source.util.JavacTask
import com.sun.tools.javac.code.Symtab
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.util.Names
import java.io.File
import java.io.StringWriter
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import javax.tools.*
import kotlin.io.path.readText

object OnTheFlyCompiler {
    private const val DEBUG_FLAG = "-g"
    private const val COMPILATION_DIR_NAME = "JavaWiz-on-the-fly-compiler"
    private val COMPILATION_OUTPUT_DIR = Files.createTempDirectory(COMPILATION_DIR_NAME).toAbsolutePath().toString()
    private val COMPILATION_INPUT_DIR = Files.createTempDirectory(COMPILATION_DIR_NAME)
    val INTERNAL_CLASS_PATTERNS: List<String> = listOf(
        "java.*", "javax.*", "javafx.*", "sun.*", "com.sun.*", "jdk.*", "*.In", "*.Out", "*.Rand", "In", "Out", "Rand"
    )


    fun compile(source: String) {
        val javac = ToolProvider.getSystemJavaCompiler()


        val fileManager = javac.getStandardFileManager(null, null, null)
        val outWriter = StringWriter()
        val diagnosticListener: DiagnosticListener<in JavaFileObject?>? = null

        val options = listOf(DEBUG_FLAG)
        val classes: List<String>? = null

        val sources = listOf(FakeJavaSourceFile(source))
        initFileManager(fileManager, source, sources)


        val task = javac.getTask(outWriter, fileManager, diagnosticListener, options, classes, sources) as JavacTask

        val trees = task.parse()
        val context = (task as com.sun.tools.javac.api.BasicJavacTask).context

        context.put(JavaFileManager::class.java, fileManager)

        Symtab.instance(context)
        TreeMaker.instance(context)
        Names.instance(context)

        trees.filterIsInstance<JCTree.JCCompilationUnit>().filter { !isInternal(it, INTERNAL_CLASS_PATTERNS) }.forEach {
            VarAssignmentVisitor.generate(it, context)
        }


        println(task)

        task.analyze() // fill symbol table

        val files = task.generate()
        files.forEach {
            println(it)
        }

        println(outWriter.toString())
    }

    private fun isInternal(compilationUnit: JCTree.JCCompilationUnit, internalClassPatterns: List<String>): Boolean {
        val packageDot = if (compilationUnit.packageName == null) "" else (compilationUnit.packageName.toString() + ".")
        val typeNames =
            compilationUnit.typeDecls.filterIsInstance<JCTree.JCClassDecl>().flatMap { getTypeNames(packageDot, it) }

        return typeNames.all { name ->
            outerClassMatchesOuterClassPattern(name, internalClassPatterns)
        }
    }

    private fun getTypeNames(packageDot: String, tree: JCTree.JCClassDecl): List<String> {
        val flatInnerClassDeclarations = tree.members.filterIsInstance<JCTree.JCClassDecl>()
        val recursiveInnerClasses = flatInnerClassDeclarations.flatMap { inner ->
            getTypeNames("", inner).map {
                packageDot + tree.name.toString() + "\$" + it
            }
        }
        return listOf(packageDot + tree.name.toString()) + recursiveInnerClasses
    }

    private fun outerClassMatchesOuterClassPattern(s: String, internalClassPatterns: List<String>): Boolean {
        return internalClassPatterns.any { pattern ->
            val outerClass = s.split("$").first()
            val outerClassPattern = pattern.split("$").first()
            when (val i = outerClassPattern.indexOf('*')) {
                -1 -> outerClass == outerClassPattern
                0 -> outerClass.endsWith(outerClassPattern.substring(1))
                else -> if (i == outerClassPattern.length - 1) outerClass.startsWith(
                    outerClassPattern.substring(
                        0,
                        outerClassPattern.length - 1
                    )
                ) else error(
                    "glob at " +
                            "beginning or end " +
                            "expected"
                )
            }
        }
    }


    private fun initFileManager(
        fileManager: StandardJavaFileManager,
        mainUri: String,
        sourceFiles: List<FakeJavaSourceFile>
    ) {
        File(COMPILATION_OUTPUT_DIR).deleteRecursively()
        Files.createDirectories(Paths.get(COMPILATION_OUTPUT_DIR))
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, listOf(File(COMPILATION_OUTPUT_DIR)))

        File(COMPILATION_INPUT_DIR.toAbsolutePath().toString()).deleteRecursively()
        Files.createDirectories(COMPILATION_INPUT_DIR)

        // consider subdirectories that contain main class (e.g. /src/) as possible sources roots
        val sourcePaths =
            listOf(COMPILATION_INPUT_DIR) + (Paths.get(mainUri).parent?.map { COMPILATION_INPUT_DIR.resolve(it) }
                ?: listOf())

        fileManager.setLocation(StandardLocation.SOURCE_PATH, sourcePaths.map { it.toFile() })

        for (fake in sourceFiles) {
            val path = Paths.get(COMPILATION_INPUT_DIR.toString(), fake.name)
            Files.createDirectories(path.parent)
            Files.write(path, fake.getCharContent(false).toString().toByteArray())
        }
    }
}

class FakeJavaSourceFile(
    uri: String,
) : SimpleJavaFileObject(
    URI.create(uri),
    JavaFileObject.Kind.SOURCE
) {
    private val content: String = Paths.get(uri).readText()

    override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
        return content
    }
}