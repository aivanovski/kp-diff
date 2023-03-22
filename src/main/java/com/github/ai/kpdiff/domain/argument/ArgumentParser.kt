package com.github.ai.kpdiff.domain.argument

import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.domain.Strings.FILE_DOES_NOT_EXIST
import com.github.ai.kpdiff.domain.Strings.MISSING_ARGUMENT
import com.github.ai.kpdiff.domain.Strings.NO_ARGUMENTS_FOUND
import com.github.ai.kpdiff.domain.Strings.UNKNOWN_ARGUMENT
import com.github.ai.kpdiff.domain.Strings.UNKNOWN_OPTION
import com.github.ai.kpdiff.entity.Arguments
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.exception.ParsingException
import java.util.LinkedList

class ArgumentParser(
    private val fsProvider: FileSystemProvider
) {

    fun parse(args: Array<String>): Either<Arguments> {
        val queue = LinkedList(args.toList())
        if (args.isEmpty()) {
            return Either.Left(ParsingException(NO_ARGUMENTS_FOUND))
        }

        var leftPath: String? = null
        var rightPath: String? = null
        var isUseOnePassword = false
        var isNoColoredOutput = false
        var keyPath: String? = null
        var leftKeyPath: String? = null
        var rightKeyPath: String? = null

        while (queue.isNotEmpty()) {
            val arg = queue.poll()
            if (arg.startsWith("-") || arg.startsWith("--")) {
                when (OPTIONAL_ARGUMENTS_MAP[arg]) {
                    OptionalArgument.ONE_PASSWORD -> {
                        isUseOnePassword = true
                    }
                    OptionalArgument.NO_COLOR -> {
                        isNoColoredOutput = true
                    }
                    OptionalArgument.KEY_FILE_A -> {
                        val path = checkPath(queue.poll())
                        if (path.isLeft()) {
                            return path.mapToLeft()
                        }

                        leftKeyPath = path.unwrap()
                    }
                    OptionalArgument.KEY_FILE_B -> {
                        val path = checkPath(queue.poll())
                        if (path.isLeft()) {
                            return path.mapToLeft()
                        }

                        rightKeyPath = path.unwrap()
                    }
                    OptionalArgument.KEY_FILE -> {
                        val path = checkPath(queue.poll())
                        if (path.isLeft()) {
                            return path.mapToLeft()
                        }

                        keyPath = path.unwrap()
                    }
                    null -> {
                        return Either.Left(
                            ParsingException(String.format(UNKNOWN_OPTION, arg))
                        )
                    }
                }
            } else {
                when {
                    leftPath == null -> {
                        val path = checkPath(arg)
                        if (path.isLeft()) {
                            return path.mapToLeft()
                        }

                        leftPath = path.unwrap()
                    }
                    rightPath == null -> {
                        val path = checkPath(arg)
                        if (path.isLeft()) {
                            return path.mapToLeft()
                        }

                        rightPath = path.unwrap()
                    }
                    else -> {
                        return Either.Left(
                            ParsingException(String.format(UNKNOWN_ARGUMENT, arg))
                        )
                    }
                }
            }
        }

        if (leftPath == null) {
            return missingArgumentError(ARGUMENT_FILE_A)
        }

        if (rightPath == null) {
            return missingArgumentError(ARGUMENT_FILE_B)
        }

        return Either.Right(
            Arguments(
                leftPath = leftPath,
                rightPath = rightPath,
                isUseOnePassword = isUseOnePassword,
                isNoColoredOutput = isNoColoredOutput,
                keyPath = keyPath,
                leftKeyPath = leftKeyPath,
                rightKeyPath = rightKeyPath
            )
        )
    }

    private fun <T> missingArgumentError(argumentName: String): Either<T> {
        return Either.Left(
            ParsingException(
                String.format(
                    MISSING_ARGUMENT,
                    argumentName
                )
            )
        )
    }

    private fun checkPath(path: String?): Either<String> {
        if (path.isNullOrEmpty() || !fsProvider.exists(path)) {
            return Either.Left(
                ParsingException(String.format(FILE_DOES_NOT_EXIST, path))
            )
        }

        return Either.Right(path)
    }

    companion object {
        internal const val ARGUMENT_FILE_A = "FILE-A"
        internal const val ARGUMENT_FILE_B = "FILE-B"

        private val OPTIONAL_ARGUMENTS_MAP = mapOf(
            OptionalArgument.HELP.cliShortName to OptionalArgument.HELP,
            OptionalArgument.HELP.cliFullName to OptionalArgument.HELP,

            OptionalArgument.ONE_PASSWORD.cliShortName to OptionalArgument.ONE_PASSWORD,
            OptionalArgument.ONE_PASSWORD.cliFullName to OptionalArgument.ONE_PASSWORD,

            OptionalArgument.KEY_FILE_A.cliShortName to OptionalArgument.KEY_FILE_A,
            OptionalArgument.KEY_FILE_A.cliFullName to OptionalArgument.KEY_FILE_A,

            OptionalArgument.KEY_FILE_B.cliShortName to OptionalArgument.KEY_FILE_B,
            OptionalArgument.KEY_FILE_B.cliFullName to OptionalArgument.KEY_FILE_B,

            OptionalArgument.KEY_FILE.cliShortName to OptionalArgument.KEY_FILE,
            OptionalArgument.KEY_FILE.cliFullName to OptionalArgument.KEY_FILE,

            OptionalArgument.NO_COLOR.cliShortName to OptionalArgument.NO_COLOR,
            OptionalArgument.NO_COLOR.cliFullName to OptionalArgument.NO_COLOR
        )
    }
}