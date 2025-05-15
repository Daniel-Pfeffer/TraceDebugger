@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")
package at.jku.ssw


fun main(){
    val filePath = (OnTheFlyCompiler::class.java).classLoader.getResource("Test.java").path
    OnTheFlyCompiler.compile(filePath)
}