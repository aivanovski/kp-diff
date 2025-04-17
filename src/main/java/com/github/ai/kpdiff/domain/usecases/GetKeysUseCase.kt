package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.entity.Arguments
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.entity.KeepassKey.FileKey
import com.github.ai.kpdiff.entity.KeepassKey.PasswordKey

class GetKeysUseCase(
    private val readPasswordUseCase: ReadPasswordUseCase
) {

    fun getKeys(args: Arguments): Either<Pair<KeepassKey, KeepassKey>> {
        val isLeftPasswordSpecified = (args.password != null || args.leftPassword != null)
        val isLeftKeyFileSpecified = (args.keyPath != null || args.leftKeyPath != null)

        val isRightPasswordSpecified = (args.password != null || args.rightPassword != null)
        val isRightKeyFileSpecified = (args.keyPath != null || args.rightKeyPath != null)

        val shouldReadLeftPassword =
            (args.isUseOnePassword || args.isAskPassword || args.isAskLeftPassword)

        val shouldReadRightPassword =
            (args.isUseOnePassword || args.isAskPassword || args.isAskRightPassword)

//        if ()

        return when {
            args.password != null -> {
                Either.Right(PasswordKey(args.password) to PasswordKey(args.password))
            }

            args.leftPassword != null && args.rightPassword != null -> {
                Either.Right(
                    PasswordKey(args.leftPassword) to PasswordKey(args.rightPassword)
                )
            }

            args.isUseOnePassword -> {
                readPasswordForBothFiles(args)
            }

            else -> {
                readPasswordsForEachFileSeparately(args)
            }
        }
    }

    private fun readPasswordForBothFiles(args: Arguments): Either<Pair<KeepassKey, KeepassKey>> {
//        val password = readPasswordUseCase.readPassword(
//            listOf(args.leftPath, args.rightPath)
//        )
//        if (password.isLeft()) {
//            return password.mapToLeft()
//        }

//        return Either.Right(PasswordKey(password.unwrap()) to PasswordKey(password.unwrap()))
        TODO()
    }

    private fun readPasswordsForEachFileSeparately(
        args: Arguments
    ): Either<Pair<KeepassKey, KeepassKey>> {
        val leftKeyPath = args.keyPath ?: args.leftKeyPath
        val rightKeyPath = args.keyPath ?: args.rightKeyPath

        val keys = mutableListOf<KeepassKey>()
        val pathToKeyPathPairs = listOf(
            Pair(args.leftPath, leftKeyPath),
            Pair(args.rightPath, rightKeyPath)
        )

        for ((path, keyPath) in pathToKeyPathPairs) {
            if (keyPath != null) {
                keys.add(FileKey(keyPath))
            } else {
                val password = readPasswordUseCase.readPassword(
                    path = path,
                    keyPath = null,
                    isPrintFileName = true
                )
                if (password.isLeft()) {
                    return password.mapToLeft()
                }

                keys.add(PasswordKey(password.unwrap()))
            }
        }

        return Either.Right(keys[0] to keys[1])
    }
}