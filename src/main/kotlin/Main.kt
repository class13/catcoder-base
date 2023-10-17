import catcoder.base.DirectoryFilesRunner

fun main(args: Array<String>) {
    DirectoryFilesRunner("directory").forEach { reader, writer ->
        writer.writeOne("test")
    }
}