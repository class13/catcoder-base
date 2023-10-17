package catcoder.base

import java.io.File

class DirectoryFilesRunner(
    val directory: String,
    val fileRegex: Regex = Regex(".*\\.in$")
) {
    fun forEach(method: (reader: CatCoderFileReader, writer: CatCoderFileWriter) -> Unit) {
        val directoryFile = File(directory)
        val files = directoryFile.listFiles { _, name -> fileRegex.matches(name) }?.toList() ?: listOf()
        files.forEach {
            CatCoderFileReader(it).use { reader ->
                CatCoderFileWriter(File(it.absolutePath + ".out")).use { writer ->
                    method(reader, writer)
                }
            }
        }
    }
}