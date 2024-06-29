package com.github.ai.kpdiff.testUtils

import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.entity.Either
import java.io.FileNotFoundException
import java.io.InputStream

class MockedFileSystemProvider(
    initialContent: Map<String, String> = emptyMap()
) : FileSystemProvider {

    private val fileContent: MutableMap<String, String> = initialContent.toMutableMap()

    override fun exists(path: String): Boolean {
        return fileContent.containsKey(path)
    }

    override fun openForRead(path: String): Either<InputStream> {
        val content = fileContent[path]
        return if (content != null) {
            Either.Right(content.toInputStream())
        } else {
            Either.Left(FileNotFoundException(path))
        }
    }

    override fun write(
        path: String,
        content: InputStream
    ): Either<Unit> {
        fileContent[path] = String(content.readAllBytes())
        return Either.Right(Unit)
    }

    fun read(path: String): String? {
        return fileContent[path]
    }
}