package com.github.ai.kpdiff.data.keepass

import app.keemobile.kotpass.errors.CryptoError
import com.github.ai.kpdiff.DatabaseFactory.FILE_KEY
import com.github.ai.kpdiff.DatabaseFactory.PASSWORD_KEY
import com.github.ai.kpdiff.DatabaseFactory.createDatabase
import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.testUtils.buildNodeTree
import com.github.ai.kpdiff.testUtils.convert
import com.github.ai.kpdiff.testUtils.isContentEquals
import com.github.ai.kpdiff.testUtils.toInputStream
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
        val testDb = createDatabase(PASSWORD_KEY)
        val key = PASSWORD_KEY.convert()
        every { fsProvider.open(PATH) }.returns(Either.Right(testDb.toInputStream()))

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, key)

        // assert
        db.isRight() shouldBe true
        db.unwrap().root.isContentEquals(testDb.buildNodeTree()) shouldBe true
    }

    @Test
    fun `createDatabase should return error if password is not correct`() {
        // arrange
        val testDb = createDatabase(PASSWORD_KEY)
        val key = KeepassKey.PasswordKey(INVALID_PASSWORD)
        every { fsProvider.open(PATH) }.returns(Either.Right(testDb.toInputStream()))

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, key)

        // assert
        db.isLeft() shouldBe true
        db.unwrapError() should beInstanceOf<CryptoError.InvalidKey>()
    }

    @Test
    fun `createDatabase should return error if db file is not exist`() {
        // arrange
        val key = PASSWORD_KEY.convert()
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
        val key = KeepassKey.FileKey(KEY_PATH)
        val testDb = createDatabase(FILE_KEY)
        every { fsProvider.open(PATH) }.returns(Either.Right(testDb.toInputStream()))
        every { fsProvider.open(KEY_PATH) }.returns(Either.Right(FILE_KEY.toInputStream()))

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, key)

        // assert
        db.isRight() shouldBe true
        db.unwrap().root.isContentEquals(testDb.buildNodeTree()) shouldBe true
    }

    @Test
    fun `createDatabase should return error if key is invalid`() {
        // arrange
        val testDb = createDatabase(FILE_KEY)
        val invalidKey = KeepassKey.FileKey(KEY_PATH)
        val keyContent = INVALID_KEY_CONTENT.byteInputStream()
        every { fsProvider.open(PATH) }.returns(Either.Right(testDb.toInputStream()))
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
        val key = KeepassKey.FileKey(KEY_PATH)
        val exception = FileNotFoundException()
        every { fsProvider.open(KEY_PATH) }.returns(Either.Left(exception))

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, key)

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