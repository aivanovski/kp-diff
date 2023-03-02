package com.github.ai.kpdiff.domain

import com.github.ai.kpdiff.data.keepass.KeepassDatabaseFactory
import com.github.ai.kpdiff.domain.usecases.ReadPasswordUseCase
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.InputReaderType
import com.github.ai.kpdiff.entity.KeepassKey

class MainInteractor(
    private val readPasswordUseCase: ReadPasswordUseCase,
    private val dbFactory: KeepassDatabaseFactory
) {

    // TODO: write tests
    fun process(args: Array<String>): Either<Unit> {
        val password = readPasswordUseCase.readPassword(InputReaderType.STANDARD)
        if (password.isLeft()) {
            return password.mapToLeft()
        }

        val key = KeepassKey.PasswordKey(password.unwrap())
        val firstDb = dbFactory.createDatabase(args[0], key)
        if (firstDb.isLeft()) {
            return firstDb.mapToLeft()
        }

        val secondDb = dbFactory.createDatabase(args[1], key)
        if (secondDb.isLeft()) {
            return secondDb.mapToLeft()
        }

        return Either.Right(Unit)
    }
}