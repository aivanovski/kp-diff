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

        while (queue.isNotEmpty()) {
            val arg = queue.poll()
            if (arg.startsWith("-") || arg.startsWith("--")) {
                val type = OPTIONAL_ARGUMENTS_MAP[arg]
                if (type == null) {
                    return Either.Left(
                        ParsingException(String.format(UNKNOWN_OPTION, arg))
                    )
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
            return Either.Left(
                ParsingException(String.format(MISSING_ARGUMENT, ARGUMENT_FILE_A))
            )
        }

        if (rightPath == null) {
            return Either.Left(
                ParsingException(String.format(MISSING_ARGUMENT, ARGUMENT_FILE_B))
            )
        }

        return Either.Right(
            Arguments(
                leftPath = leftPath,
                rightPath = rightPath
            )
        )
    }

    private fun checkPath(path: String): Either<String> {
        if (!fsProvider.exists(path)) {
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
            OptionalArgument.HELP.cliFullName to OptionalArgument.HELP
        )
    }
}