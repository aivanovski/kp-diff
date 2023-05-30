package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.data.keepass.KeepassDatabaseFactory
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.KeepassKey

class OpenDatabasesUseCase(
    private val dbFactory: KeepassDatabaseFactory,
) {

    fun openDatabases(
        leftPath: String,
        leftKey: KeepassKey,
        rightPath: String,
        rightKey: KeepassKey
    ): Either<Pair<KeepassDatabase, KeepassDatabase>> {
        val lhs = dbFactory.createDatabase(leftPath, leftKey)
        if (lhs.isLeft()) {
            return lhs.mapToLeft()
        }

        val rhs = dbFactory.createDatabase(rightPath, rightKey)
        if (rhs.isLeft()) {
            return rhs.mapToLeft()
        }

        return Either.Right(lhs.unwrap() to rhs.unwrap())
    }
}