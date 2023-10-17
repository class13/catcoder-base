package catcoder.base

import java.io.Closeable
import java.io.File
import java.io.FileWriter

class CatCoderFileWriter(
    file: File,
): Closeable {
    val fileWriter = FileWriter(file)

    fun writeOne(value: String) {
        fileWriter.write(value)
        fileWriter.write("\n")
        fileWriter.flush()
    }

    fun writeOne(value: List<Any>, separator: String = " ") {
        writeOne(value.joinToString(separator))
    }

    fun writeMany(values: List<List<Any>>, separator: String = " ") {
        values.forEach {
            writeOne(it, separator)
        }
    }

    fun writeMany(values: List<String>) {
        values.forEach {
            writeOne(it)
        }
    }

    override fun close() {
        fileWriter.close()
    }

}