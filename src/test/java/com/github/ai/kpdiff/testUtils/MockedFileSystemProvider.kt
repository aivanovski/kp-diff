package com.github.ai.kpdiff.testUtils

import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.utils.StringUtils
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.InputStream

class MockedFileSystemProvider(
    content: Map<String, ByteArray> = emptyMap()
) : FileSystemProvider {

    private val fileContent: MutableMap<String, ByteArray> = content.toMutableMap()

    override fun getName(path: String): Either<String> {
        return if (fileContent.containsKey(path)) {
            val lastSlashIndex = path.lastIndexOf("/")

            val name = when {
                lastSlashIndex >= 0 && lastSlashIndex + 1 < path.length -> {
                    path.substring(lastSlashIndex + 1)
                }

                lastSlashIndex >= 0 -> StringUtils.EMPTY

                else -> path
            }

            Either.Right(name)
        } else {
            Either.Left(FileNotFoundException())
        }
    }

    override fun exists(path: String): Boolean {
        return fileContent.containsKey(path)
    }

    override fun openForRead(path: String): Either<InputStream> {
        val content = fileContent[path]
        return if (content != null) {
            Either.Right(ByteArrayInputStream(content))
        } else {
            Either.Left(FileNotFoundException(path))
        }
    }

    override fun write(
        path: String,
        content: InputStream
    ): Either<Unit> {
        fileContent[path] = content.readAllBytes()
        return Either.Right(Unit)
    }

    fun read(path: String): String? {
        val content = fileContent[path]
        return content?.let { String(it) }
    }
}