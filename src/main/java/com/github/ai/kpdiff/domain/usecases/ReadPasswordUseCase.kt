package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.data.keepass.KeepassDatabaseFactory
import com.github.ai.kpdiff.domain.ErrorHandler
import com.github.ai.kpdiff.domain.Strings.ENTER_A_PASSWORD_FOR_FILE
import com.github.ai.kpdiff.domain.Strings.TOO_MANY_ATTEMPTS
import com.github.ai.kpdiff.domain.input.InputReaderFactory
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.entity.exception.KpDiffException
import java.io.File

class ReadPasswordUseCase(
    private val determinInputTypeUseCase: DetermineInputTypeUseCase,
    private val dbFactory: KeepassDatabaseFactory,
    private val inputReaderFactory: InputReaderFactory,
    private val errorHandler: ErrorHandler,
    private val printer: OutputPrinter
) {

    fun readPassword(filePath: String): Either<String> {
        val filename = File(filePath).name

        val inputType = determinInputTypeUseCase.getInputReaderType()
        val inputReader = inputReaderFactory.createReader(inputType)
        for (attempt in 1..MAX_ATTEMPTS) {
            printer.printLine(String.format(ENTER_A_PASSWORD_FOR_FILE, filename))

            val password = inputReader.read()
            val db = dbFactory.createDatabase(filePath, KeepassKey.PasswordKey(password))
            if (db.isRight()) {
                return Either.Right(password)
            } else {
                errorHandler.handleIfLeft(db)
            }
        }

        return Either.Left(KpDiffException(TOO_MANY_ATTEMPTS))
    }

    companion object {
        internal const val MAX_ATTEMPTS = 3
    }
}