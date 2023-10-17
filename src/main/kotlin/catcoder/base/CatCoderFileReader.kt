package catcoder.base

import java.io.BufferedReader
import java.io.Closeable
import java.io.File
import java.io.FileReader

class CatCoderFileReader(
    val file: File,
    val fileReader: BufferedReader = BufferedReader(FileReader(file))
): Closeable {

    fun readOne(separator: String = " "): List<String> {
        val line = fileReader.readLine()
        return line.split(separator)
    }

    fun readOne(propertyNames: List<String>, separator: String = " "): Map<String, String> {
        val lineParts = readOne(separator)

        assert(lineParts.size == propertyNames.size) { "propertyNames is invalid" }

        return propertyNames.mapIndexed { index, s -> Pair(s, lineParts[index]) }.toMap()
    }

    fun readMultiple(propertyNames: List<String>, count: Int, separator: String = " "): List<Map<String, String>> {
        return (0..<count).map {
            readOne(propertyNames, separator)
        }.toList()
    }

    fun readMultipleFromOne(propertyNames: List<String>, separator: String = " "): List<Map<String, String>> {
        val parts = readOne(separator).withIndex().groupBy { it.index / propertyNames.size }.map { it.value.map { indexedValue -> indexedValue.value }  }
        return parts.map {
            propertyNames.mapIndexed { index, s -> Pair(s, it[index]) }.toMap()
        }.toList()

    }

    override fun close() {
        fileReader.close()
    }
}