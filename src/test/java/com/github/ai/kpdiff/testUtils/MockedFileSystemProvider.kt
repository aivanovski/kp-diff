package com.github.ai.kpdiff.testUtils

import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.entity.Either
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.InputStream

class MockedFileSystemProvider(
    private val fileContent: Map<String, String> = emptyMap()
) : FileSystemProvider {

    override fun exists(path: String): Boolean {
        return fileContent.containsKey(path)
    }

    override fun open(path: String): Either<InputStream> {
        val content = fileContent[path]
        return if (content != null) {
            Either.Right(ByteArrayInputStream(content.toByteArray()))
        } else {
            Either.Left(FileNotFoundException(path))
        }
    }
}