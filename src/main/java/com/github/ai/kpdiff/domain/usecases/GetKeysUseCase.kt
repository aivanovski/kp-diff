package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.entity.Arguments
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.entity.KeepassKey.CompositeKey
import com.github.ai.kpdiff.entity.KeepassKey.FileKey
import com.github.ai.kpdiff.entity.KeepassKey.PasswordKey

class GetKeysUseCase(
    private val readPasswordUseCase: ReadPasswordUseCase
) {

    fun getKeys(args: Arguments): Either<Pair<KeepassKey, KeepassKey>> {
        val shouldReadLeftPassword =
            (args.isUseOnePassword || args.isAskPassword || args.isAskLeftPassword)

        val shouldReadRightPassword =
            (args.isUseOnePassword || args.isAskPassword || args.isAskRightPassword)

        val shouldPrintFileName =
            (!args.isUseOnePassword && !args.isAskPassword)

        val leftPassword = choosePasswordForFile(
            isPasswordInputRequested = shouldReadLeftPassword,
            pathToFile = args.leftPath,
            pathToKeyFile = args.leftKeyPath ?: args.keyPath,
            specifiedPassword = args.leftPassword ?: args.password,
            isPrintFileName = shouldPrintFileName
        )
        if (leftPassword.isLeft()) {
            return leftPassword.mapToLeft()
        }

        val rightPassword = if (args.isUseOnePassword) {
            leftPassword
        } else {
            val rightPassword = choosePasswordForFile(
                isPasswordInputRequested = shouldReadRightPassword,
                pathToFile = args.rightPath,
                pathToKeyFile = args.rightKeyPath ?: args.keyPath,
                specifiedPassword = args.rightPassword ?: args.password,
                isPrintFileName = shouldPrintFileName
            )
            if (rightPassword.isLeft()) {
                return rightPassword.mapToLeft()
            }

            rightPassword
        }

        val leftKey = createKey(
            keyPath = args.keyPath ?: args.leftKeyPath,
            password = leftPassword.unwrap()
        )
        if (leftKey.isLeft()) {
            return leftKey.mapToLeft()
        }

        val rightKey = createKey(
            keyPath = args.keyPath ?: args.rightKeyPath,
            password = rightPassword.unwrap()
        )
        if (rightKey.isLeft()) {
            return rightKey.mapToLeft()
        }

        return Either.Right(leftKey.unwrap() to rightKey.unwrap())
    }

    private fun createKey(
        keyPath: String?,
        password: String?
    ): Either<KeepassKey> {
        return when {
            keyPath != null && password != null -> Either.Right(CompositeKey(keyPath, password))
            keyPath != null -> Either.Right(FileKey(keyPath))
            password != null -> Either.Right(PasswordKey(password))
            else -> Either.Left(IllegalStateException("Unreachable branch"))
        }
    }

    private fun choosePasswordForFile(
        isPasswordInputRequested: Boolean,
        pathToFile: String,
        pathToKeyFile: String?,
        specifiedPassword: String?,
        isPrintFileName: Boolean
    ): Either<String?> {
        return when {
            isPasswordInputRequested || (pathToKeyFile == null && specifiedPassword == null) -> {
                val password = readPasswordUseCase.readPassword(
                    path = pathToFile,
                    keyPath = pathToKeyFile,
                    isPrintFileName = isPrintFileName
                )

                if (password.isLeft()) {
                    return password.mapToLeft()
                }

                password
            }

            specifiedPassword != null -> Either.Right(specifiedPassword)

            else -> Either.Right(null)
        }
    }
}