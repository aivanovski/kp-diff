package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.TestData.LEFT_FILE_PATH
import com.github.ai.kpdiff.TestData.LEFT_PASSWORD
import com.github.ai.kpdiff.TestData.RIGHT_FILE_PATH
import com.github.ai.kpdiff.TestData.RIGHT_PASSWORD
import com.github.ai.kpdiff.data.keepass.KeepassDatabaseFactory
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.KeepassKey.PasswordKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class OpenDatabasesUseCaseTest {

    private val dbFactory = mockk<KeepassDatabaseFactory>()
    private val leftDb = mockk<KeepassDatabase>()
    private val rightDb = mockk<KeepassDatabase>()

    @Test
    fun `openDatabases return databases`() {
        // arrange
        val leftKey = PasswordKey(LEFT_PASSWORD)
        val rightKey = PasswordKey(RIGHT_PASSWORD)
        every { dbFactory.createDatabase(LEFT_FILE_PATH, leftKey) }.returns(Either.Right(leftDb))
        every { dbFactory.createDatabase(RIGHT_FILE_PATH, rightKey) }.returns(Either.Right(rightDb))

        // act
        val result = newUseCase().openDatabases(
            LEFT_FILE_PATH,
            leftKey,
            RIGHT_FILE_PATH,
            rightKey
        )

        // assert
        result shouldBe Either.Right(leftDb to rightDb)
    }

    @Test
    fun `openDatabases should return error if left database cannot be opened`() {
        val leftKey = PasswordKey(LEFT_PASSWORD)
        val rightKey = PasswordKey(RIGHT_PASSWORD)
        every { dbFactory.createDatabase(LEFT_FILE_PATH, leftKey) }
            .returns(Either.Left(EXCEPTION))

        val result = newUseCase().openDatabases(
            LEFT_FILE_PATH,
            leftKey,
            RIGHT_FILE_PATH,
            rightKey
        )

        result shouldBe Either.Left(EXCEPTION)
    }

    @Test
    fun `openDatabase should return error if right database cannot be opened`() {
        val leftKey = PasswordKey(LEFT_PASSWORD)
        val rightKey = PasswordKey(RIGHT_PASSWORD)
        every { dbFactory.createDatabase(LEFT_FILE_PATH, leftKey) }
            .returns(Either.Right(leftDb))
        every { dbFactory.createDatabase(RIGHT_FILE_PATH, rightKey) }
            .returns(Either.Left(EXCEPTION))

        val result = newUseCase().openDatabases(
            LEFT_FILE_PATH,
            leftKey,
            RIGHT_FILE_PATH,
            rightKey
        )

        result shouldBe Either.Left(EXCEPTION)
    }

    private fun newUseCase(): OpenDatabasesUseCase =
        OpenDatabasesUseCase(
            dbFactory = dbFactory
        )

    companion object {
        private val EXCEPTION = Exception()
    }
}