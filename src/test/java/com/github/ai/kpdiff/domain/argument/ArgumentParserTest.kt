package com.github.ai.kpdiff.domain.argument

import com.github.ai.kpdiff.TestData.FILE_CONTENT
import com.github.ai.kpdiff.TestData.LEFT_FILE_PATH
import com.github.ai.kpdiff.TestData.RIGHT_FILE_PATH
import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.domain.Strings.FILE_DOES_NOT_EXIST
import com.github.ai.kpdiff.domain.Strings.MISSING_ARGUMENT
import com.github.ai.kpdiff.domain.Strings.NO_ARGUMENTS_FOUND
import com.github.ai.kpdiff.domain.Strings.UNKNOWN_ARGUMENT
import com.github.ai.kpdiff.domain.Strings.UNKNOWN_OPTION
import com.github.ai.kpdiff.domain.argument.ArgumentParser.Companion.ARGUMENT_FILE_A
import com.github.ai.kpdiff.domain.argument.ArgumentParser.Companion.ARGUMENT_FILE_B
import com.github.ai.kpdiff.entity.Arguments
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
        val result = ArgumentParser(newMockedProviderWithFiles())
            .parse(arrayOf(LEFT_FILE_PATH, RIGHT_FILE_PATH))

        // assert
        result.isRight() shouldBe true
        result.unwrap() shouldBe expected
    }

    @Test
    fun `parse should return error if no arguments provided`() {
        // arrange
        val message = NO_ARGUMENTS_FOUND

        // act
        val result = ArgumentParser(newEmptyProvider())
            .parse(emptyArray())

        // assert
        result.isLeft() shouldBe true
        result.unwrapError() should beInstanceOf<ParsingException>()
        result.unwrapError().message shouldBe message
    }

    @Test
    fun `parse should return error if right left is not specified`() {
        // arrange
        val message = String.format(MISSING_ARGUMENT, ARGUMENT_FILE_A)
        val args = arrayOf(
            OptionalArgument.HELP.cliFullName
        )

        // act
        val result = ArgumentParser(newMockedProviderWithFiles())
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
        val result = ArgumentParser(newMockedProviderWithFiles())
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
        val result = ArgumentParser(newMockedProviderWithFiles())
            .parse(args)

        // assert
        result.isLeft() shouldBe true
        result.unwrapError() should beInstanceOf<ParsingException>()
        result.unwrapError().message shouldBe message
    }

    @Test
    fun `parse should return arguments if --help is specified`() {
        // arrange
        val expected = newArguments(LEFT_FILE_PATH, RIGHT_FILE_PATH)
        val args = arrayOf(
            LEFT_FILE_PATH,
            RIGHT_FILE_PATH,
            OptionalArgument.HELP.cliFullName
        )

        // act
        val result = ArgumentParser(newMockedProviderWithFiles())
            .parse(args)

        // assert
        result.isRight() shouldBe true
        result.unwrap() shouldBe expected
    }

    @Test
    fun `parse should return arguments if -h is specified`() {
        // arrange
        val expected = newArguments(LEFT_FILE_PATH, RIGHT_FILE_PATH)
        val args = arrayOf(
            LEFT_FILE_PATH,
            RIGHT_FILE_PATH,
            OptionalArgument.HELP.cliShortName
        )

        // act
        val result = ArgumentParser(newMockedProviderWithFiles())
            .parse(args)

        // assert
        result.isRight() shouldBe true
        result.unwrap() shouldBe expected
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
        val result = ArgumentParser(newMockedProviderWithFiles())
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
        val result = ArgumentParser(newMockedProviderWithFiles())
            .parse(args)

        // assert
        result.isRight() shouldBe true
        result.unwrap() shouldBe expected
    }

    private fun newEmptyProvider(): FileSystemProvider {
        return MockedFileSystemProvider()
    }

    private fun newArguments(
        leftPath: String = EMPTY,
        rightPath: String = EMPTY,
        isUseOnePassword: Boolean = false
    ): Arguments {
        return Arguments(
            leftPath,
            rightPath,
            isUseOnePassword
        )
    }

    private fun newMockedProviderWithFiles(): FileSystemProvider {
        return MockedFileSystemProvider(
            fileContent = mapOf(
                LEFT_FILE_PATH to FILE_CONTENT,
                RIGHT_FILE_PATH to FILE_CONTENT
            )
        )
    }

    companion object {
        private const val UNKNOWN = "--unknown"
        private const val THIRD_FILE_PATH = "/path/to/third.kdbx"
    }
}