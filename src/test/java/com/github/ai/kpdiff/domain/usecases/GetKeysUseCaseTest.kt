package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.TestData.KEY_PATH
import com.github.ai.kpdiff.TestData.LEFT_FILE_PATH
import com.github.ai.kpdiff.TestData.LEFT_KEY_PATH
import com.github.ai.kpdiff.TestData.LEFT_PASSWORD
import com.github.ai.kpdiff.TestData.PASSWORD
import com.github.ai.kpdiff.TestData.RIGHT_FILE_PATH
import com.github.ai.kpdiff.TestData.RIGHT_KEY_PATH
import com.github.ai.kpdiff.TestData.RIGHT_PASSWORD
import com.github.ai.kpdiff.entity.Arguments
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassKey.FileKey
import com.github.ai.kpdiff.entity.KeepassKey.PasswordKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import java.lang.Exception
import org.junit.jupiter.api.Test

class GetKeysUseCaseTest {

    private val readPasswordUseCase = mockk<ReadPasswordUseCase>()

    @Test
    fun `getKeys should return passwords if isUseOnePassword is specified`() {
        // arrange
        val args = newArgs(isUseOnePassword = true)
        val expected = Pair(PasswordKey(PASSWORD), PasswordKey(PASSWORD))
        every { readPasswordUseCase.readPassword(listOf(LEFT_FILE_PATH, RIGHT_FILE_PATH)) }
            .returns(Either.Right(PASSWORD))

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Right(expected)
    }

    @Test
    fun `getKeys should return error if isUseOnePassword is specified`() {
        // arrange
        val args = newArgs(isUseOnePassword = true)
        every { readPasswordUseCase.readPassword(listOf(LEFT_FILE_PATH, RIGHT_FILE_PATH)) }
            .returns(Either.Left(EXCEPTION))

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Left(EXCEPTION)
    }

    @Test
    fun `getKeys should return different passwords`() {
        // arrange
        val args = newArgs()
        val expected = Pair(PasswordKey(LEFT_PASSWORD), PasswordKey(RIGHT_PASSWORD))
        every { readPasswordUseCase.readPassword(listOf(LEFT_FILE_PATH)) }
            .returns(Either.Right(LEFT_PASSWORD))
        every { readPasswordUseCase.readPassword(listOf(RIGHT_FILE_PATH)) }
            .returns(Either.Right(RIGHT_PASSWORD))

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Right(expected)
    }

    @Test
    fun `getKeys should return error if unable to get password for left`() {
        // arrange
        every { readPasswordUseCase.readPassword(listOf(LEFT_FILE_PATH)) }
            .returns(Either.Left(EXCEPTION))

        // act
        val result = newUseCase().getKeys(newArgs())

        // assert
        result shouldBe Either.Left(EXCEPTION)
    }

    @Test
    fun `getKeys should return error if unable to get password for right`() {
        // arrange
        every { readPasswordUseCase.readPassword(listOf(LEFT_FILE_PATH)) }
            .returns(Either.Right(RIGHT_PASSWORD))
        every { readPasswordUseCase.readPassword(listOf(RIGHT_FILE_PATH)) }
            .returns(Either.Left(EXCEPTION))

        // act
        val result = newUseCase().getKeys(newArgs())

        // assert
        verifySequence {
            readPasswordUseCase.readPassword(listOf(LEFT_FILE_PATH))
            readPasswordUseCase.readPassword(listOf(RIGHT_FILE_PATH))
        }
        result shouldBe Either.Left(EXCEPTION)
    }

    @Test
    fun `getKeys should return file keys`() {
        // arrange
        val args = newArgs(
            leftKeyPath = LEFT_KEY_PATH,
            rightKeyPath = RIGHT_KEY_PATH
        )
        val expected = Pair(FileKey(LEFT_KEY_PATH), FileKey(RIGHT_KEY_PATH))

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Right(expected)
    }

    @Test
    fun `getKeys should return file keys with the same path`() {
        // arrange
        val args = newArgs(keyPath = KEY_PATH)
        val expected = Pair(FileKey(KEY_PATH), FileKey(KEY_PATH))

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Right(expected)
    }

    private fun newUseCase(): GetKeysUseCase {
        return GetKeysUseCase(readPasswordUseCase)
    }

    private fun newArgs(
        leftPath: String = LEFT_FILE_PATH,
        rightPath: String = RIGHT_FILE_PATH,
        keyPath: String? = null,
        leftKeyPath: String? = null,
        rightKeyPath: String? = null,
        isUseOnePassword: Boolean = false
    ): Arguments {
        return Arguments(
            leftPath = leftPath,
            rightPath = rightPath,
            isUseOnePassword = isUseOnePassword,
            isNoColoredOutput = false,
            keyPath = keyPath,
            leftKeyPath = leftKeyPath,
            rightKeyPath = rightKeyPath,
            isPrintHelp = false,
            isPrintVersion = false,
            isVerboseOutput = false
        )
    }

    companion object {
        private val EXCEPTION = Exception()
    }
}