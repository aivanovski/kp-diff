package com.github.ai.kpdiff.domain.argument

import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.domain.Strings.FILE_DOES_NOT_EXIST
import com.github.ai.kpdiff.domain.Strings.ILLEGAL_ARGUMENT_VALUE
import com.github.ai.kpdiff.domain.Strings.MISSING_ARGUMENT
import com.github.ai.kpdiff.domain.Strings.MISSING_ARGUMENT_VALUE
import com.github.ai.kpdiff.domain.Strings.UNKNOWN_ARGUMENT
import com.github.ai.kpdiff.domain.Strings.UNKNOWN_OPTION
import com.github.ai.kpdiff.entity.Arguments
import com.github.ai.kpdiff.entity.DifferType
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.exception.ParsingException
import com.github.ai.kpdiff.utils.StringUtils.EMPTY
import java.util.LinkedList

class ArgumentParser(
    private val fsProvider: FileSystemProvider
) {

    fun parse(args: Array<String>): Either<Arguments> {
        val queue = LinkedList(args.toList())
        if (args.isEmpty()) {
            return Either.Right(
                Arguments(
                    leftPath = EMPTY,
                    rightPath = EMPTY,
                    keyPath = null,
                    leftKeyPath = null,
                    rightKeyPath = null,
                    differType = null,
                    isUseOnePassword = false,
                    isNoColoredOutput = false,
                    isPrintHelp = true,
                    isPrintVersion = false,
                    isVerboseOutput = false
                )
            )
        }

        var leftPath: String? = null
        var rightPath: String? = null
        var keyPath: String? = null
        var leftKeyPath: String? = null
        var rightKeyPath: String? = null
        var differType: DifferType? = null
        var isUseOnePassword = false
        var isNoColoredOutput = false
        var isPrintHelp = false
        var isPrintVersion = false
        var isVerboseOutput = false

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

                    OptionalArgument.HELP -> {
                        isPrintHelp = true
                    }

                    OptionalArgument.VERSION -> {
                        isPrintVersion = true
                    }

                    OptionalArgument.VERBOSE -> {
                        isVerboseOutput = true
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

                    OptionalArgument.DIFF_BY -> {
                        val differTypeResult = parseDifferType(queue.poll())
                        if (differTypeResult.isLeft()) {
                            return differTypeResult.mapToLeft()
                        }

                        differType = differTypeResult.unwrap()
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

        if (leftPath == null && !isPrintHelp && !isPrintVersion) {
            return missingArgumentError(ARGUMENT_FILE_A)
        }

        if (rightPath == null && !isPrintHelp && !isPrintVersion) {
            return missingArgumentError(ARGUMENT_FILE_B)
        }

        return Either.Right(
            Arguments(
                leftPath = leftPath ?: EMPTY,
                rightPath = rightPath ?: EMPTY,
                keyPath = keyPath,
                leftKeyPath = leftKeyPath,
                rightKeyPath = rightKeyPath,
                differType = differType,
                isUseOnePassword = isUseOnePassword,
                isNoColoredOutput = isNoColoredOutput,
                isPrintHelp = isPrintHelp,
                isPrintVersion = isPrintVersion,
                isVerboseOutput = isVerboseOutput
            )
        )
    }

    private fun parseDifferType(value: String?): Either<DifferType> {
        if (value.isNullOrEmpty()) {
            return missingArgumentValue(OptionalArgument.DIFF_BY.cliFullName)
        }

        return when (value.lowercase()) {
            DifferType.PATH.cliName -> Either.Right(DifferType.PATH)
            DifferType.UUID.cliName -> Either.Right(DifferType.UUID)
            else -> illegalArgumentValue(
                argumentName = OptionalArgument.DIFF_BY.cliFullName,
                argumentValue = value
            )
        }
    }

    private fun <T> illegalArgumentValue(
        argumentName: String,
        argumentValue: String?
    ): Either<T> {
        return Either.Left(
            ParsingException(
                String.format(
                    ILLEGAL_ARGUMENT_VALUE,
                    argumentName,
                    argumentValue
                )
            )
        )
    }

    private fun <T> missingArgumentValue(argumentName: String): Either<T> {
        return Either.Left(
            ParsingException(
                String.format(
                    MISSING_ARGUMENT_VALUE,
                    argumentName
                )
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

            OptionalArgument.VERSION.cliShortName to OptionalArgument.VERSION,
            OptionalArgument.VERSION.cliFullName to OptionalArgument.VERSION,

            OptionalArgument.ONE_PASSWORD.cliShortName to OptionalArgument.ONE_PASSWORD,
            OptionalArgument.ONE_PASSWORD.cliFullName to OptionalArgument.ONE_PASSWORD,

            OptionalArgument.KEY_FILE_A.cliShortName to OptionalArgument.KEY_FILE_A,
            OptionalArgument.KEY_FILE_A.cliFullName to OptionalArgument.KEY_FILE_A,

            OptionalArgument.KEY_FILE_B.cliShortName to OptionalArgument.KEY_FILE_B,
            OptionalArgument.KEY_FILE_B.cliFullName to OptionalArgument.KEY_FILE_B,

            OptionalArgument.KEY_FILE.cliShortName to OptionalArgument.KEY_FILE,
            OptionalArgument.KEY_FILE.cliFullName to OptionalArgument.KEY_FILE,

            OptionalArgument.NO_COLOR.cliShortName to OptionalArgument.NO_COLOR,
            OptionalArgument.NO_COLOR.cliFullName to OptionalArgument.NO_COLOR,

            OptionalArgument.VERBOSE.cliShortName to OptionalArgument.VERBOSE,
            OptionalArgument.VERBOSE.cliFullName to OptionalArgument.VERBOSE,

            OptionalArgument.DIFF_BY.cliShortName to OptionalArgument.DIFF_BY,
            OptionalArgument.DIFF_BY.cliFullName to OptionalArgument.DIFF_BY
        )
    }
}