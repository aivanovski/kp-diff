package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.data.keepass.KeepassDatabaseFactory
import com.github.ai.kpdiff.domain.ErrorHandler
import com.github.ai.kpdiff.domain.Strings.ENTER_A_PASSWORD
import com.github.ai.kpdiff.domain.Strings.ENTER_A_PASSWORD_FOR_FILE
import com.github.ai.kpdiff.domain.input.InputReaderFactory
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassKey.CompositeKey
import com.github.ai.kpdiff.entity.KeepassKey.PasswordKey
import com.github.ai.kpdiff.entity.exception.TooManyAttemptsException

class ReadPasswordUseCase(
    private val fileSystemProvider: FileSystemProvider,
    private val determineInputTypeUseCase: DetermineInputTypeUseCase,
    private val dbFactory: KeepassDatabaseFactory,
    private val inputReaderFactory: InputReaderFactory,
    private val errorHandler: ErrorHandler,
    private val printer: OutputPrinter
) {

    fun readPassword(
        path: String,
        keyPath: String?,
        isPrintFileName: Boolean
    ): Either<String> {
        val getFileNameResult = fileSystemProvider.getName(path)
        if (getFileNameResult.isLeft()) {
            return getFileNameResult.mapToLeft()
        }

        val fileName = getFileNameResult.unwrap()

        val inputType = determineInputTypeUseCase.getInputReaderType()
        val inputReader = inputReaderFactory.createReader(inputType)
        for (i in 1..MAX_ATTEMPTS) {
            val message = if (isPrintFileName) {
                String.format(
                    ENTER_A_PASSWORD_FOR_FILE,
                    fileName
                )
            } else {
                ENTER_A_PASSWORD
            }

            printer.printLine(message)

            val password = checkPassword(
                path = path,
                keyPath = keyPath,
                password = inputReader.read()
            )
            if (password.isRight()) {
                return password
            } else {
                errorHandler.handleIfLeft(password)
            }
        }

        return Either.Left(TooManyAttemptsException())
    }

    private fun checkPassword(
        path: String,
        keyPath: String?,
        password: String
    ): Either<String> {
        val key = if (keyPath != null) {
            CompositeKey(
                path = keyPath,
                password = password
            )
        } else {
            PasswordKey(
                password = password
            )
        }

        val db = dbFactory.createDatabase(path, key)
        if (db.isLeft()) {
            return db.mapToLeft()
        }

        return Either.Right(password)
    }

    companion object {
        internal const val MAX_ATTEMPTS = 3
    }
}