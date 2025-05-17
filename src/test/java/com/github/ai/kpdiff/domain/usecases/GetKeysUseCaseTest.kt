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
import com.github.ai.kpdiff.entity.KeepassKey.CompositeKey
import com.github.ai.kpdiff.entity.KeepassKey.FileKey
import com.github.ai.kpdiff.entity.KeepassKey.PasswordKey
import com.github.ai.kpdiff.entity.exception.TooManyAttemptsException
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class GetKeysUseCaseTest {

    private val readPasswordUseCase = mockk<ReadPasswordUseCase>()

    @Test
    fun `getKeys should return one password if it is specified as argument`() {
        // arrange
        val args = newArgs(password = PASSWORD)
        val expected = PasswordKey(PASSWORD) to PasswordKey(PASSWORD)

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Right(expected)
    }

    @Test
    fun `getKeys should return two passwords if they are specified as arguments`() {
        // arrange
        val args = newArgs(
            leftPassword = LEFT_PASSWORD,
            rightPassword = RIGHT_PASSWORD
        )

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Right(PasswordKey(LEFT_PASSWORD) to PasswordKey(RIGHT_PASSWORD))
    }

    @Test
    fun `getKeys should read one password if isUseOnePassword is specified`() {
        // TODO: add error scenario

        // arrange
        val args = newArgs(
            isUseOnePassword = true
        )
        every {
            readPasswordUseCase.readPassword(
                path = LEFT_FILE_PATH,
                keyPath = null,
                isPrintFileName = false
            )
        }.returns(Either.Right(PASSWORD))

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Right(PasswordKey(PASSWORD) to PasswordKey(PASSWORD))
    }

    @Test
    fun `getKeys should fail if unable to read password and isUseOnePassword is specified`() {
        // arrange
        val args = newArgs(
            isUseOnePassword = true
        )
        val exception = TooManyAttemptsException()
        every {
            readPasswordUseCase.readPassword(
                path = LEFT_FILE_PATH,
                keyPath = null,
                isPrintFileName = false
            )
        }.returns(Either.Left(exception))

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Left(exception)
    }

    @Test
    fun `getKeys should read passwords for both files`() {
        // arrange
        val args = newArgs()
        every {
            readPasswordUseCase.readPassword(
                path = LEFT_FILE_PATH,
                keyPath = null,
                isPrintFileName = true
            )
        }.returns(Either.Right(LEFT_PASSWORD))
        every {
            readPasswordUseCase.readPassword(
                path = RIGHT_FILE_PATH,
                keyPath = null,
                isPrintFileName = true
            )
        }.returns(Either.Right(RIGHT_PASSWORD))

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Right(PasswordKey(LEFT_PASSWORD) to PasswordKey(RIGHT_PASSWORD))
    }

    @Test
    fun `getKeys should fail if unable to read left password`() {
        // arrange
        val args = newArgs()
        val exception = TooManyAttemptsException()
        every {
            readPasswordUseCase.readPassword(
                path = LEFT_FILE_PATH,
                keyPath = null,
                isPrintFileName = true
            )
        }.returns(Either.Left(exception))

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Left(exception)
    }

    @Test
    fun `getKeys should fail if unable to read right password`() {
        // arrange
        val args = newArgs()
        val exception = TooManyAttemptsException()
        every {
            readPasswordUseCase.readPassword(
                path = LEFT_FILE_PATH,
                keyPath = null,
                isPrintFileName = true
            )
        }.returns(Either.Right(LEFT_PASSWORD))
        every {
            readPasswordUseCase.readPassword(
                path = RIGHT_FILE_PATH,
                keyPath = null,
                isPrintFileName = true
            )
        }.returns(Either.Left(exception))

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Left(exception)
    }

    @Test
    fun `getKeys should return two file keys`() {
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
    fun `getKeys should return one file key for both file`() {
        // arrange
        val args = newArgs(keyPath = KEY_PATH)

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Right(FileKey(KEY_PATH) to FileKey(KEY_PATH))
    }

    @Test
    fun `getKeys should return one composite key for both files if password is specified`() {
        // arrange
        val args = newArgs(keyPath = KEY_PATH, password = PASSWORD)
        val expected =
            CompositeKey(KEY_PATH, PASSWORD) to CompositeKey(KEY_PATH, PASSWORD)

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Right(expected)
    }

    @Test
    fun `getKeys should return two composite keys if passwords are specified`() {
        // arrange
        val args = newArgs(
            leftKeyPath = LEFT_KEY_PATH,
            rightKeyPath = RIGHT_KEY_PATH,
            leftPassword = LEFT_PASSWORD,
            rightPassword = RIGHT_PASSWORD
        )
        val expected = CompositeKey(LEFT_KEY_PATH, LEFT_PASSWORD) to CompositeKey(
            RIGHT_KEY_PATH,
            RIGHT_PASSWORD
        )

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Right(expected)
    }

    @Test
    fun `getKeys should return two composite keys if password input is requested for both`() {
        // arrange
        val args = newArgs(
            keyPath = KEY_PATH,
            isAskPassword = true
        )
        val expected = CompositeKey(KEY_PATH, PASSWORD) to CompositeKey(KEY_PATH, PASSWORD)
        every {
            readPasswordUseCase.readPassword(
                path = LEFT_FILE_PATH,
                keyPath = KEY_PATH,
                isPrintFileName = false
            )
        }.returns(Either.Right(PASSWORD))
        every {
            readPasswordUseCase.readPassword(
                path = RIGHT_FILE_PATH,
                keyPath = KEY_PATH,
                isPrintFileName = false
            )
        }.returns(Either.Right(PASSWORD))

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Right(expected)
    }

    @Test
    fun `getKeys should return composite key for left and file key for left`() {
        // arrange
        val args = newArgs(
            keyPath = KEY_PATH,
            isAskLeftPassword = true
        )
        val expected = CompositeKey(KEY_PATH, PASSWORD) to FileKey(KEY_PATH)
        every {
            readPasswordUseCase.readPassword(
                path = LEFT_FILE_PATH,
                keyPath = KEY_PATH,
                isPrintFileName = true
            )
        }.returns(Either.Right(PASSWORD))

        // act
        val result = newUseCase().getKeys(args)

        // assert
        result shouldBe Either.Right(expected)
    }

    @Test
    fun `getKeys should return composite key for right and file key for right`() {
        // arrange
        val args = newArgs(
            keyPath = KEY_PATH,
            isAskRightPassword = true
        )
        val expected = FileKey(KEY_PATH) to CompositeKey(KEY_PATH, PASSWORD)
        every {
            readPasswordUseCase.readPassword(
                path = RIGHT_FILE_PATH,
                keyPath = KEY_PATH,
                isPrintFileName = true
            )
        }.returns(Either.Right(PASSWORD))

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
        password: String? = null,
        leftPassword: String? = null,
        rightPassword: String? = null,
        isUseOnePassword: Boolean = false,
        isAskPassword: Boolean = false,
        isAskLeftPassword: Boolean = false,
        isAskRightPassword: Boolean = false
    ): Arguments {
        return Arguments(
            leftPath = leftPath,
            rightPath = rightPath,
            isNoColoredOutput = false,
            keyPath = keyPath,
            leftKeyPath = leftKeyPath,
            rightKeyPath = rightKeyPath,
            password = password,
            leftPassword = leftPassword,
            rightPassword = rightPassword,
            differType = null,
            outputFilePath = null,
            isUseOnePassword = isUseOnePassword,
            isAskPassword = isAskPassword,
            isAskLeftPassword = isAskLeftPassword,
            isAskRightPassword = isAskRightPassword,
            isPrintHelp = false,
            isPrintVersion = false,
            isVerboseOutput = false
        )
    }
}