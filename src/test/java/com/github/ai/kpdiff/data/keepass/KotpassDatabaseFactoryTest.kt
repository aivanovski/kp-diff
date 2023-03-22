package com.github.ai.kpdiff.data.keepass

import com.github.ai.kpdiff.TestData
import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.testUtils.asFileKey
import com.github.ai.kpdiff.testUtils.contentStream
import com.github.ai.kpdiff.testUtils.convert
import com.github.ai.kpdiff.testUtils.isContentEquals
import io.github.anvell.kotpass.errors.CryptoError
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.every
import io.mockk.mockk
import java.io.FileNotFoundException
import org.junit.jupiter.api.Test

internal class KotpassDatabaseFactoryTest {

    private val fsProvider = mockk<FileSystemProvider>()

    @Test
    fun `createDatabase should read database with password`() {
        // arrange
        val testDb = TestData.DB_WITH_PASSWORD
        val key = testDb.key.convert()
        every { fsProvider.open(PATH) }.returns(Either.Right(testDb.contentStream()))

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, key)

        // assert
        db.isRight() shouldBe true
        db.unwrap().root.isContentEquals(testDb) shouldBe true
    }

    @Test
    fun `createDatabase should return error if password is not correct`() {
        // arrange
        val testDb = TestData.DB_WITH_PASSWORD
        val key = KeepassKey.PasswordKey(INVALID_PASSWORD)
        every { fsProvider.open(PATH) }.returns(Either.Right(testDb.contentStream()))

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, key)

        // assert
        db.isLeft() shouldBe true
        db.unwrapError() should beInstanceOf<CryptoError.InvalidKey>()
    }

    @Test
    fun `createDatabase should return error if db file is not exist`() {
        // arrange
        val testDb = TestData.DB_WITH_PASSWORD
        val key = testDb.key.convert()
        val exception = FileNotFoundException()
        every { fsProvider.open(PATH) }.returns(Either.Left(exception))

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, key)

        // assert
        db.isLeft() shouldBe true
        db.unwrapError() should beInstanceOf<FileNotFoundException>()
    }

    @Test
    fun `createDatabase should read database with key file`() {
        // arrange
        val testDb = TestData.DB_WITH_KEY
        val key = testDb.key.asFileKey()
        val kotpassKey = key.convert()
        every { fsProvider.open(PATH) }.returns(Either.Right(testDb.contentStream()))
        every { fsProvider.open(key.path) }.returns(Either.Right(key.contentStream()))

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, kotpassKey)

        // assert
        db.isRight() shouldBe true
        db.unwrap().root.isContentEquals(testDb) shouldBe true
    }

    @Test
    fun `createDatabase should return error if key is invalid`() {
        // arrange
        val testDb = TestData.DB_WITH_KEY
        val invalidKey = KeepassKey.FileKey(KEY_PATH)
        val keyContent = INVALID_KEY_CONTENT.byteInputStream()
        every { fsProvider.open(PATH) }.returns(Either.Right(testDb.contentStream()))
        every { fsProvider.open(KEY_PATH) }.returns(Either.Right(keyContent))

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, invalidKey)

        // assert
        db.isLeft() shouldBe true
        db.unwrapError() should beInstanceOf<CryptoError.InvalidKey>()
    }

    @Test
    fun `createDatabase should return error if key file is missing`() {
        // arrange
        val testDb = TestData.DB_WITH_KEY
        val key = testDb.key.asFileKey()
        val kotpassKey = key.convert()
        val exception = FileNotFoundException()
        every { fsProvider.open(key.path) }.returns(Either.Left(exception))

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, kotpassKey)

        // assert
        db.isLeft() shouldBe true
        db.unwrapError() should beInstanceOf<FileNotFoundException>()
    }

    companion object {
        private const val PATH = "db.kdbx"
        private const val KEY_PATH = "key-path"
        private const val INVALID_PASSWORD = "123"
        private const val INVALID_KEY_CONTENT = "invalid-key-content"
    }
}