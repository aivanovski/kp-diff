package com.github.ai.kpdiff.testUtils

import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.entity.Either
import java.io.InputStream

/**
 * Provides files from src/test/resources folder
 */
class ResourcesFileSystemProvider : FileSystemProvider {

    override fun exists(path: String): Boolean {
        val stream = this.javaClass.classLoader.getResourceAsStream(path)
        return stream != null
    }

    override fun open(path: String): Either<InputStream> {
        return Either.Right(resourceAsStream(path))
    }
}