package catcoder.base

import java.io.File

class ResourceUtil {

    companion object {
        fun getFileFromResource(path: String): File {
            return ResourceUtil::class.java.classLoader.getResource(path).let { File(it!!.toURI()) }
        }
    }
}