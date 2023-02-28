package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.domain.input.InputReaderFactory
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.InputReaderType

class ReadPasswordUseCase(
    private val inputReaderFactory: InputReaderFactory
) {

    fun readPassword(inputReaderType: InputReaderType): Either<String> {
        val reader = inputReaderFactory.createReader(inputReaderType)
        return Either.Right(reader.read())
    }
}