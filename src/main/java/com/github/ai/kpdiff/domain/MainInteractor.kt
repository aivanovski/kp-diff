package com.github.ai.kpdiff.domain

import com.github.ai.kpdiff.domain.usecases.ReadPasswordUseCase
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.InputReaderType

class MainInteractor(
    private val readPasswordUseCase: ReadPasswordUseCase
) {

    // TODO: write tests
    fun process(args: Array<String>): Either<Unit> {
        val password = readPasswordUseCase.readPassword(InputReaderType.STANDARD)
        if (password.isLeft()) {
            return password.mapToLeft()
        }

        return Either.Right(Unit)
    }
}