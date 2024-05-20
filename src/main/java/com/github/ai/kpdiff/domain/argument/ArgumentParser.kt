package com.github.ai.kpdiff.domain.argument

import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.domain.Strings.FILE_DOES_NOT_EXIST
import com.github.ai.kpdiff.domain.Strings.ILLEGAL_ARGUMENT_VALUE
import com.github.ai.kpdiff.domain.Strings.MISSING_ARGUMENT
import com.github.ai.kpdiff.domain.Strings.MISSING_ARGUMENT_VALUE
import com.github.ai.kpdiff.domain.Strings.UNKNOWN_ARGUMENT
import com.github.ai.kpdiff.domain.Strings.UNKNOWN_OPTION
import com.github.ai.kpdiff.entity.Arguments
import com.github.ai.kpdiff.entity.Arguments.Companion.EMPTY_ARGUMENTS
import com.github.ai.kpdiff.entity.DifferType
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.MutableArguments
import com.github.ai.kpdiff.entity.exception.ParsingException
import java.util.LinkedList
import java.util.Queue

class ArgumentParser(
    private val fsProvider: FileSystemProvider
) {

    fun parse(args: Array<String>): Either<Arguments> {
        val queue = LinkedList(args.toList())
        if (args.isEmpty()) {
            return Either.Right(
                EMPTY_ARGUMENTS.copy(
                    isPrintHelp = true
                )
            )
        }

        val values = MutableArguments()

        while (queue.isNotEmpty()) {
            val result = parseNextArgument(queue, values)
            if (result.isLeft()) {
                return result.mapToLeft()
            }
        }

        if (values.leftPath == null && !values.isPrintHelp && !values.isPrintVersion) {
            return missingArgumentError(ARGUMENT_FILE_A)
        }

        if (values.rightPath == null &&
            !values.isPrintHelp &&
            !values.isPrintVersion
        ) {
            return missingArgumentError(ARGUMENT_FILE_B)
        }

        return Either.Right(values.toArguments())
    }

    private fun parseNextArgument(
        queue: Queue<String>,
        values: MutableArguments
    ): Either<Unit> {
        val argument = queue.poll()
        val isOption = argument.startsWith("-")

        return when {
            isOption -> parseOption(argument, queue, values)
            values.leftPath == null -> parseLeftPath(argument, values)
            values.rightPath == null -> parseRightPath(argument, values)

            else -> {
                Either.Left(
                    ParsingException(String.format(UNKNOWN_ARGUMENT, argument))
                )
            }
        }
    }

    private fun parseOption(
        name: String,
        queue: Queue<String>,
        values: MutableArguments
    ): Either<Unit> {
        val type = OPTIONAL_ARGUMENT_NAMES[name]

        return when (type) {
            OptionalArgument.ONE_PASSWORD -> parseOnePassword(values)
            OptionalArgument.NO_COLOR -> parseNoColor(values)
            OptionalArgument.HELP -> parseHelp(values)
            OptionalArgument.VERSION -> parseVersion(values)
            OptionalArgument.VERBOSE -> parseVerbose(values)
            OptionalArgument.KEY_FILE_A -> parseLeftKeyPath(queue.poll(), values)
            OptionalArgument.KEY_FILE_B -> parseRightKeyPath(queue.poll(), values)
            OptionalArgument.PASSWORD -> parsePassword(queue.poll(), values)
            OptionalArgument.KEY_FILE -> parseKeyPath(queue.poll(), values)
            OptionalArgument.DIFF_BY -> parseDifferType(queue.poll(), values)
            OptionalArgument.OUTPUT_FILE -> parseFormatPatch(queue.poll(), values)
            else -> {
                Either.Left(
                    ParsingException(String.format(UNKNOWN_OPTION, name))
                )
            }
        }
    }

    private fun parseOnePassword(arguments: MutableArguments): Either<Unit> {
        arguments.isUseOnePassword = true
        return Either.Right(Unit)
    }

    private fun parseNoColor(arguments: MutableArguments): Either<Unit> {
        arguments.isNoColoredOutput = true
        return Either.Right(Unit)
    }

    private fun parseHelp(arguments: MutableArguments): Either<Unit> {
        arguments.isPrintHelp = true
        return Either.Right(Unit)
    }

    private fun parseVersion(arguments: MutableArguments): Either<Unit> {
        arguments.isPrintVersion = true
        return Either.Right(Unit)
    }

    private fun parseVerbose(arguments: MutableArguments): Either<Unit> {
        arguments.isVerboseOutput = true
        return Either.Right(Unit)
    }

    private fun parseLeftKeyPath(
        path: String?,
        arguments: MutableArguments
    ): Either<Unit> {
        val checkPathResult = checkPath(path)

        if (checkPathResult.isRight()) {
            arguments.leftKeyPath = checkPathResult.unwrap()
        }

        return checkPathResult.mapWith(Unit)
    }

    private fun parseRightKeyPath(
        path: String?,
        arguments: MutableArguments
    ): Either<Unit> {
        val checkPathResult = checkPath(path)

        if (checkPathResult.isRight()) {
            arguments.rightKeyPath = checkPathResult.unwrap()
        }

        return checkPathResult.mapWith(Unit)
    }

    private fun parseKeyPath(
        path: String?,
        arguments: MutableArguments
    ): Either<Unit> {
        val checkPathResult = checkPath(path)

        if (checkPathResult.isRight()) {
            arguments.keyPath = checkPathResult.unwrap()
        }

        return checkPathResult.mapWith(Unit)
    }

    private fun parsePassword(
        password: String?,
        arguments: MutableArguments
    ): Either<Unit> {
        if (password.isNullOrBlank()) {
            return missingArgumentValue(OptionalArgument.PASSWORD.cliFullName)
        }

        arguments.password = password

        return Either.Right(Unit)
    }

    private fun parseLeftPath(
        path: String?,
        arguments: MutableArguments
    ): Either<Unit> {
        val checkPathResult = checkPath(path)

        if (checkPathResult.isRight()) {
            arguments.leftPath = checkPathResult.unwrap()
        }

        return checkPathResult.mapWith(Unit)
    }

    private fun parseRightPath(
        path: String?,
        arguments: MutableArguments
    ): Either<Unit> {
        val checkPathResult = checkPath(path)

        if (checkPathResult.isRight()) {
            arguments.rightPath = checkPathResult.unwrap()
        }

        return checkPathResult.mapWith(Unit)
    }

    private fun parseDifferType(
        value: String?,
        arguments: MutableArguments
    ): Either<Unit> {
        if (value.isNullOrEmpty()) {
            return missingArgumentValue(OptionalArgument.DIFF_BY.cliFullName)
        }

        arguments.differType = when (value.lowercase()) {
            DifferType.PATH.cliName -> DifferType.PATH
            DifferType.UUID.cliName -> DifferType.UUID
            else -> null
        }

        return if (arguments.differType != null) {
            Either.Right(Unit)
        } else {
            illegalArgumentValue(
                argumentName = OptionalArgument.DIFF_BY.cliFullName,
                argumentValue = value
            )
        }
    }

    private fun parseFormatPatch(
        value: String?,
        arguments: MutableArguments
    ): Either<Unit> {
        if (value.isNullOrEmpty()) {
            return missingArgumentValue(OptionalArgument.OUTPUT_FILE.cliFullName)
        }

        arguments.outputFilePath = value

        return Either.Right(Unit)
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

        private val OPTIONAL_ARGUMENT_NAMES = OptionalArgument.values()
            .flatMap { argument ->
                listOf(
                    argument.cliShortName to argument,
                    argument.cliFullName to argument
                )
            }
            .toMap()
    }
}