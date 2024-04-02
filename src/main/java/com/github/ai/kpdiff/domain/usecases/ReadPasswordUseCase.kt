package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.data.keepass.KeepassDatabaseFactory
import com.github.ai.kpdiff.domain.ErrorHandler
import com.github.ai.kpdiff.domain.Strings.ENTER_A_PASSWORD
import com.github.ai.kpdiff.domain.Strings.ENTER_A_PASSWORD_FOR_FILE
import com.github.ai.kpdiff.domain.Strings.TOO_MANY_ATTEMPTS
import com.github.ai.kpdiff.domain.input.InputReaderFactory
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.entity.exception.KpDiffException
import java.io.File

class ReadPasswordUseCase(
    private val determineInputTypeUseCase: DetermineInputTypeUseCase,
    private val dbFactory: KeepassDatabaseFactory,
    private val inputReaderFactory: InputReaderFactory,
    private val errorHandler: ErrorHandler,
    private val printer: OutputPrinter
) {

    fun readPassword(
        paths: List<String>
    ): Either<String> {
        val filenames = paths.map { path -> File(path).name }

        val inputType = determineInputTypeUseCase.getInputReaderType()
        val inputReader = inputReaderFactory.createReader(inputType)
        (1..MAX_ATTEMPTS).forEach { _ ->
            if (paths.size == 1) {
                printer.printLine(
                    String.format(
                        ENTER_A_PASSWORD_FOR_FILE,
                        filenames.first()
                    )
                )
            } else {
                printer.printLine(ENTER_A_PASSWORD)
            }

            val password = checkPassword(paths, inputReader.read())
            if (password.isRight()) {
                return password
            } else {
                errorHandler.handleIfLeft(password)
            }
        }

        return Either.Left(KpDiffException(TOO_MANY_ATTEMPTS))
    }

    private fun checkPassword(paths: List<String>, password: String): Either<String> {
        for (path in paths) {
            val db = dbFactory.createDatabase(path, KeepassKey.PasswordKey(password))
            if (db.isLeft()) {
                return db.mapToLeft()
            }
        }

        return Either.Right(password)
    }

    companion object {
        internal const val MAX_ATTEMPTS = 3
    }
}