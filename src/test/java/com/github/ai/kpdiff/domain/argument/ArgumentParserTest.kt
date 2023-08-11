package com.github.ai.kpdiff.domain.argument

import com.github.ai.kpdiff.TestData
import com.github.ai.kpdiff.TestData.FILE_CONTENT
import com.github.ai.kpdiff.TestData.INVALID
import com.github.ai.kpdiff.TestData.LEFT_FILE_PATH
import com.github.ai.kpdiff.TestData.LEFT_KEY_PATH
import com.github.ai.kpdiff.TestData.RIGHT_FILE_PATH
import com.github.ai.kpdiff.TestData.RIGHT_KEY_PATH
import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.domain.Strings
import com.github.ai.kpdiff.domain.Strings.FILE_DOES_NOT_EXIST
import com.github.ai.kpdiff.domain.Strings.ILLEGAL_ARGUMENT_VALUE
import com.github.ai.kpdiff.domain.Strings.MISSING_ARGUMENT
import com.github.ai.kpdiff.domain.Strings.MISSING_ARGUMENT_VALUE
import com.github.ai.kpdiff.domain.Strings.UNKNOWN_ARGUMENT
import com.github.ai.kpdiff.domain.Strings.UNKNOWN_OPTION
import com.github.ai.kpdiff.domain.argument.ArgumentParser.Companion.ARGUMENT_FILE_A
import com.github.ai.kpdiff.domain.argument.ArgumentParser.Companion.ARGUMENT_FILE_B
import com.github.ai.kpdiff.domain.argument.ArgumentParserTest.Side.BOTH
import com.github.ai.kpdiff.domain.argument.ArgumentParserTest.Side.LEFT
import com.github.ai.kpdiff.domain.argument.ArgumentParserTest.Side.RIGHT
import com.github.ai.kpdiff.entity.Arguments
import com.github.ai.kpdiff.entity.DifferType
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.exception.ParsingException
import com.github.ai.kpdiff.testUtils.MockedFileSystemProvider
import com.github.ai.kpdiff.utils.StringUtils.EMPTY
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import org.junit.jupiter.api.Test

internal class ArgumentParserTest {

    @Test
    fun `parse should return arguments`() {
        // arrange
        val expected = newArguments(LEFT_FILE_PATH, RIGHT_FILE_PATH)

        // act
        val result = ArgumentParser(newMockedProviderWithAllFiles())
            .parse(arrayOf(LEFT_FILE_PATH, RIGHT_FILE_PATH))

        // assert
        result.isRight() shouldBe true
        result.unwrap() shouldBe expected
    }

    @Test
    fun `parse should set isPrintHelp if no arguments provided`() {
        // arrange
        val expected = newArguments(isPrintHelp = true)

        // act
        val result = ArgumentParser(newEmptyProvider())
            .parse(emptyArray())

        // assert
        result shouldBe Either.Right(expected)
    }

    @Test
    fun `parse should return error if left file is not specified`() {
        // arrange
        val message = String.format(MISSING_ARGUMENT, ARGUMENT_FILE_A)
        val args = arrayOf(
            OptionalArgument.ONE_PASSWORD.cliFullName
        )

        // act
        val result = ArgumentParser(newMockedProviderWithAllFiles())
            .parse(args)

        // assert
        result.isLeft() shouldBe true
        result.unwrapError() should beInstanceOf<ParsingException>()
        result.unwrapError().message shouldBe message
    }

    @Test
    fun `parse should return error if right file is not specified`() {
        // arrange
        val message = String.format(MISSING_ARGUMENT, ARGUMENT_FILE_B)

        // act
        val result = ArgumentParser(newMockedProviderWithAllFiles())
            .parse(arrayOf(LEFT_FILE_PATH))

        // assert
        result.isLeft() shouldBe true
        result.unwrapError() should beInstanceOf<ParsingException>()
        result.unwrapError().message shouldBe message
    }

    @Test
    fun `parse should return error if left file does not exist`() {
        // arrange
        val message = String.format(FILE_DOES_NOT_EXIST, LEFT_FILE_PATH)
        val fsProvider = MockedFileSystemProvider(
            fileContent = mapOf(
                RIGHT_FILE_PATH to EMPTY
            )
        )

        // act
        val result = ArgumentParser(fsProvider)
            .parse(arrayOf(LEFT_FILE_PATH, RIGHT_FILE_PATH))

        // assert
        result.isLeft() shouldBe true
        result.unwrapError() should beInstanceOf<ParsingException>()
        result.unwrapError().message shouldBe message
    }

    @Test
    fun `parse should return error if right file does not exist`() {
        // arrange
        val message = String.format(FILE_DOES_NOT_EXIST, RIGHT_FILE_PATH)
        val fsProvider = MockedFileSystemProvider(
            fileContent = mapOf(
                LEFT_FILE_PATH to EMPTY
            )
        )

        // act
        val result = ArgumentParser(fsProvider)
            .parse(arrayOf(LEFT_FILE_PATH, RIGHT_FILE_PATH))

        // assert
        result.isLeft() shouldBe true
        result.unwrapError() should beInstanceOf<ParsingException>()
        result.unwrapError().message shouldBe message
    }

    @Test
    fun `parse should return error if unknown argument specified`() {
        // arrange
        val message = String.format(UNKNOWN_OPTION, UNKNOWN)

        // act
        val result = ArgumentParser(newEmptyProvider())
            .parse(arrayOf(UNKNOWN))

        // assert
        result.isLeft() shouldBe true
        result.unwrapError() should beInstanceOf<ParsingException>()
        result.unwrapError().message shouldBe message
    }

    @Test
    fun `parse should return error if too many arguments specified`() {
        // arrange
        val message = String.format(UNKNOWN_ARGUMENT, THIRD_FILE_PATH)
        val args = arrayOf(
            LEFT_FILE_PATH,
            RIGHT_FILE_PATH,
            THIRD_FILE_PATH
        )

        // act
        val result = ArgumentParser(newMockedProviderWithAllFiles())
            .parse(args)

        // assert
        result.isLeft() shouldBe true
        result.unwrapError() should beInstanceOf<ParsingException>()
        result.unwrapError().message shouldBe message
    }

    @Test
    fun `parse should return arguments if --help is specified`() {
        listOf(
            OptionalArgument.HELP.cliFullName,
            OptionalArgument.HELP.cliShortName
        ).forEach { argumentName ->
            // arrange
            val expected = newArguments(isPrintHelp = true)
            val args = arrayOf(argumentName)

            // act
            val result = ArgumentParser(newMockedProviderWithAllFiles())
                .parse(args)

            // assert
            result shouldBe Either.Right(expected)
        }
    }

    @Test
    fun `parse should return arguments if --version is specified`() {
        listOf(
            OptionalArgument.VERSION.cliFullName,
            OptionalArgument.VERSION.cliShortName
        ).forEach { argumentName ->
            // arrange
            val expected = newArguments(isPrintVersion = true)
            val args = arrayOf(argumentName)

            // act
            val result = ArgumentParser(newMockedProviderWithAllFiles())
                .parse(args)

            // assert
            result shouldBe Either.Right(expected)
        }
    }

    @Test
    fun `parse should return arguments if --verbose is specified`() {
        listOf(
            OptionalArgument.VERBOSE.cliFullName,
            OptionalArgument.VERBOSE.cliShortName
        ).forEach { argumentName ->
            // arrange
            val expected = newArguments(LEFT_FILE_PATH, RIGHT_FILE_PATH, isVerboseOutput = true)
            val args = arrayOf(
                LEFT_FILE_PATH,
                RIGHT_FILE_PATH,
                argumentName
            )

            // act
            val result = ArgumentParser(newMockedProviderWithAllFiles())
                .parse(args)

            // assert
            result shouldBe Either.Right(expected)
        }
    }

    @Test
    fun `parse should return arguments if --one-password is specified`() {
        // arrange
        val expected = newArguments(LEFT_FILE_PATH, RIGHT_FILE_PATH, isUseOnePassword = true)
        val args = arrayOf(
            LEFT_FILE_PATH,
            RIGHT_FILE_PATH,
            OptionalArgument.ONE_PASSWORD.cliFullName
        )

        // act
        val result = ArgumentParser(newMockedProviderWithAllFiles())
            .parse(args)

        // assert
        result.isRight() shouldBe true
        result.unwrap() shouldBe expected
    }

    @Test
    fun `parse should return arguments if -o is specified`() {
        // arrange
        val expected = newArguments(LEFT_FILE_PATH, RIGHT_FILE_PATH, isUseOnePassword = true)
        val args = arrayOf(
            LEFT_FILE_PATH,
            RIGHT_FILE_PATH,
            OptionalArgument.ONE_PASSWORD.cliShortName
        )

        // act
        val result = ArgumentParser(newMockedProviderWithAllFiles())
            .parse(args)

        // assert
        result.isRight() shouldBe true
        result.unwrap() shouldBe expected
    }

    @Test
    fun `parse should return key path if it is specified`() {
        listOf(
            Triple(BOTH, OptionalArgument.KEY_FILE.cliFullName, LEFT_KEY_PATH),
            Triple(BOTH, OptionalArgument.KEY_FILE.cliShortName, LEFT_KEY_PATH),
            Triple(LEFT, OptionalArgument.KEY_FILE_A.cliFullName, LEFT_KEY_PATH),
            Triple(LEFT, OptionalArgument.KEY_FILE_A.cliShortName, LEFT_KEY_PATH),
            Triple(RIGHT, OptionalArgument.KEY_FILE_B.cliFullName, RIGHT_KEY_PATH),
            Triple(RIGHT, OptionalArgument.KEY_FILE_B.cliShortName, RIGHT_KEY_PATH)
        ).forEach { (side, argumentName, keyPath) ->
            // arrange
            val expected = newArguments(
                LEFT_FILE_PATH,
                RIGHT_FILE_PATH,
                keyPath = if (side == BOTH) keyPath else null,
                leftKeyPath = if (side == LEFT) keyPath else null,
                rightKeyPath = if (side == RIGHT) keyPath else null
            )

            // act
            val result = ArgumentParser(newMockedProviderWithAllFiles())
                .parse(
                    args = arrayOf(
                        LEFT_FILE_PATH,
                        RIGHT_FILE_PATH,
                        argumentName,
                        keyPath
                    )
                )

            // assert
            result.isRight() shouldBe true
            result.unwrap() shouldBe expected
        }
    }

    @Test
    fun `parse should return error if key file is not found`() {
        val fsProvider = MockedFileSystemProvider(
            fileContent = mapOf(
                LEFT_FILE_PATH to FILE_CONTENT,
                RIGHT_FILE_PATH to FILE_CONTENT
            )
        )

        listOf(
            Pair(OptionalArgument.KEY_FILE.cliFullName, LEFT_KEY_PATH),
            Pair(OptionalArgument.KEY_FILE_A.cliFullName, LEFT_KEY_PATH),
            Pair(OptionalArgument.KEY_FILE_B.cliFullName, RIGHT_KEY_PATH)
        ).forEach { (argumentName, keyPath) ->
            // arrange
            val message = String.format(FILE_DOES_NOT_EXIST, keyPath)

            // act
            val result = ArgumentParser(fsProvider)
                .parse(
                    args = arrayOf(
                        LEFT_FILE_PATH,
                        RIGHT_FILE_PATH,
                        argumentName,
                        keyPath
                    )
                )

            // assert
            result.isLeft() shouldBe true
            result.unwrapError() should beInstanceOf<ParsingException>()
            result.unwrapError().message shouldBe message
        }
    }

    @Test
    fun `parse should return arguments if no-color option is specified`() {
        listOf(
            OptionalArgument.NO_COLOR.cliShortName,
            OptionalArgument.NO_COLOR.cliFullName
        )
            .forEach { argumentName ->
                val expected = newArguments(
                    LEFT_FILE_PATH,
                    RIGHT_FILE_PATH,
                    isNoColoredOutput = true
                )
                val args = arrayOf(
                    LEFT_FILE_PATH,
                    RIGHT_FILE_PATH,
                    argumentName
                )

                // act
                val result = ArgumentParser(newMockedProviderWithAllFiles()).parse(args)

                // assert
                result.isRight() shouldBe true
                result.unwrap() shouldBe expected
            }
    }

    @Test
    fun `parse should return arguments if --diff-by is specified`() {
        listOf(
            Pair(
                OptionalArgument.DIFF_BY.cliShortName,
                DifferType.UUID.cliName
            ) to DifferType.UUID,

            Pair(
                OptionalArgument.DIFF_BY.cliFullName,
                DifferType.UUID.cliName
            ) to DifferType.UUID,

            Pair(
                OptionalArgument.DIFF_BY.cliShortName,
                DifferType.PATH.cliName
            ) to DifferType.PATH,

            Pair(
                OptionalArgument.DIFF_BY.cliFullName,
                DifferType.PATH.cliName
            ) to DifferType.PATH,

            //
            Pair(
                OptionalArgument.DIFF_BY.cliFullName,
                DifferType.UUID.cliName.uppercase()
            ) to DifferType.UUID,

            Pair(
                OptionalArgument.DIFF_BY.cliFullName,
                DifferType.PATH.cliName.uppercase()
            ) to DifferType.PATH,
        )
            .forEach { (input, expectedDifferType) ->
                // arrange
                val (argumentName, argumentValue) = input
                val expected = newArguments(
                    LEFT_FILE_PATH,
                    RIGHT_FILE_PATH,
                    differType = expectedDifferType
                )
                val args = arrayOf(
                    LEFT_FILE_PATH,
                    RIGHT_FILE_PATH,
                    argumentName,
                    argumentValue
                )

                // act
                val result = ArgumentParser(newMockedProviderWithAllFiles()).parse(args)

                // assert
                result shouldBe Either.Right(expected)
            }
    }

    @Test
    fun `parse should return error if --diff-by value is invalid`() {
        // arrange
        val message = ILLEGAL_ARGUMENT_VALUE.format(
            OptionalArgument.DIFF_BY.cliFullName,
            INVALID
        )
        val args = arrayOf(
            LEFT_FILE_PATH,
            RIGHT_FILE_PATH,
            OptionalArgument.DIFF_BY.cliFullName,
            INVALID
        )

        // act
        val result = ArgumentParser(newMockedProviderWithAllFiles()).parse(args)

        // assert
        result.isLeft() shouldBe true
        result.unwrapError() should beInstanceOf<ParsingException>()
        result.unwrapError().message shouldBe message
    }

    @Test
    fun `parse should return error if --diff-by value is not specified`() {
        listOf(
            null,
            EMPTY
        ).forEach { argumentValue ->
            // arrange
            val message = MISSING_ARGUMENT_VALUE.format(
                OptionalArgument.DIFF_BY.cliFullName,
            )
            val args = mutableListOf(
                LEFT_FILE_PATH,
                RIGHT_FILE_PATH,
                OptionalArgument.DIFF_BY.cliFullName
            )
                .apply {
                    if (argumentValue != null) {
                        add(argumentValue)
                    }
                }
                .toTypedArray()

            // act
            val result = ArgumentParser(newMockedProviderWithAllFiles()).parse(args)

            // assert
            result.isLeft() shouldBe true
            result.unwrapError() should beInstanceOf<ParsingException>()
            result.unwrapError().message shouldBe message
        }
    }

    private fun newEmptyProvider(): FileSystemProvider {
        return MockedFileSystemProvider()
    }

    private fun newArguments(
        leftPath: String = EMPTY,
        rightPath: String = EMPTY,
        keyPath: String? = null,
        leftKeyPath: String? = null,
        rightKeyPath: String? = null,
        differType: DifferType? = null,
        isUseOnePassword: Boolean = false,
        isNoColoredOutput: Boolean = false,
        isPrintHelp: Boolean = false,
        isPrintVersion: Boolean = false,
        isVerboseOutput: Boolean = false
    ): Arguments {
        return Arguments(
            leftPath = leftPath,
            rightPath = rightPath,
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
    }

    private fun newMockedProviderWithAllFiles(): FileSystemProvider {
        return MockedFileSystemProvider(
            fileContent = mapOf(
                LEFT_FILE_PATH to FILE_CONTENT,
                RIGHT_FILE_PATH to FILE_CONTENT,
                LEFT_KEY_PATH to FILE_CONTENT,
                RIGHT_KEY_PATH to FILE_CONTENT
            )
        )
    }

    enum class Side {
        BOTH,
        LEFT,
        RIGHT
    }

    companion object {
        private const val UNKNOWN = "--unknown"
        private const val THIRD_FILE_PATH = "/path/to/third.kdbx"
    }
}