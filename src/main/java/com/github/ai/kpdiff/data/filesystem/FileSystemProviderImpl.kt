package com.github.ai.kpdiff.data.filesystem

import com.github.ai.kpdiff.entity.Either
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class FileSystemProviderImpl : FileSystemProvider {

    override fun exists(path: String): Boolean {
        return File(path).exists()
    }

    override fun open(path: String): Either<InputStream> {
        return try {
            Either.Right(FileInputStream(File(path)))
        } catch (exception: IOException) {
            Either.Left(exception)
        }
    }
}