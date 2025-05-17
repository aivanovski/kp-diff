package com.github.ai.kpdiff.data.filesystem

import com.github.ai.kpdiff.domain.Strings.FILE_DOES_NOT_EXIST
import com.github.ai.kpdiff.domain.Strings.UNABLE_TO_CREATE_DIRECTORY
import com.github.ai.kpdiff.entity.Either
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class FileSystemProviderImpl(
    private val fileFactory: FileFactory
) : FileSystemProvider {

    override fun getName(path: String): Either<String> {
        return if (exists(path)) {
            Either.Right(fileFactory.newFile(path).name)
        } else {
            Either.Left(FileNotFoundException(FILE_DOES_NOT_EXIST))
        }
    }

    override fun exists(path: String): Boolean {
        return fileFactory.newFile(path).exists()
    }

    override fun openForRead(path: String): Either<InputStream> {
        return try {
            Either.Right(FileInputStream(fileFactory.newFile(path)))
        } catch (exception: IOException) {
            Either.Left(exception)
        }
    }

    override fun write(
        path: String,
        content: InputStream
    ): Either<Unit> {
        val file = fileFactory.newFile(path)
        val parent = file.parentFile

        if (!parent.exists() && !parent.mkdirs()) {
            return Either.Left(IOException(UNABLE_TO_CREATE_DIRECTORY.format(parent.path)))
        }

        return try {
            FileOutputStream(file).use { out ->
                content.copyTo(out)
            }

            Either.Right(Unit)
        } catch (exception: IOException) {
            Either.Left(exception)
        }
    }
}