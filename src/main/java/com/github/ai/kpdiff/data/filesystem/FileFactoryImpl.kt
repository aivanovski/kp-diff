package com.github.ai.kpdiff.data.filesystem

import java.io.File

class FileFactoryImpl : FileFactory {

    override fun newFile(path: String): File {
        return File(fixPath(path))
    }

    private fun fixPath(path: String): String {
        return if (path.isNotEmpty() &&
            !path.startsWith("/") &&
            !path.startsWith("./")
        ) {
            "./$path"
        } else {
            path
        }
    }
}