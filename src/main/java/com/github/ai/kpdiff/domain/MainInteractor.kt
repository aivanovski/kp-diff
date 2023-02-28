package com.github.ai.kpdiff.domain

import com.github.ai.kpdiff.domain.usecases.ReadPasswordUseCase
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.InputReaderType

class MainInteractor(
    private val readPasswordUseCase: ReadPasswordUseCase
) {

    fun process(args: Array<String>): Either<Unit> {
        val password = readPasswordUseCase.readPassword(InputReaderType.STANDARD)
        return Either.Right(Unit)
    }
}