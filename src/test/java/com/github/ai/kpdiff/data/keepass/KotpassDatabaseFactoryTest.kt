package com.github.ai.kpdiff.data.keepass

import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.errors.CryptoError
import app.keemobile.kotpass.models.DatabaseElement
import com.github.ai.kpdiff.DatabaseFactory.DEFAULT_PASSWORD
import com.github.ai.kpdiff.DatabaseFactory.COMPOSITE_KEY
import com.github.ai.kpdiff.DatabaseFactory.FILE_KEY
import com.github.ai.kpdiff.DatabaseFactory.PASSWORD_KEY
import com.github.ai.kpdiff.DatabaseFactory.createDatabase
import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.testUtils.MockedFileSystemProvider
import com.github.ai.kpdiff.testUtils.buildNodeTree
import com.github.ai.kpdiff.testUtils.convert
import com.github.ai.kpdiff.testUtils.isContentEquals
import com.github.aivanovski.keepasstreebuilder.extensions.toByteArray
import com.github.aivanovski.keepasstreebuilder.model.Database
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import java.io.FileNotFoundException
import org.junit.jupiter.api.Test

internal class KotpassDatabaseFactoryTest {

    @Test
    fun `createDatabase should read database with password`() {
        // arrange
        val testDb = createDatabase(PASSWORD_KEY)
        val key = PASSWORD_KEY.convert()
        val fsProvider = newFsProviderWithDatabase(testDb)

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
        val fsProvider = newFsProviderWithDatabase(testDb)

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
        val fsProvider = newEmptyFsProvider()

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
        val fsProvider = MockedFileSystemProvider(
            content = mapOf(
                PATH to testDb.toByteArray(),
                KEY_PATH to FILE_KEY.binaryData
            )
        )

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
        val fsProvider = MockedFileSystemProvider(
            content = mapOf(
                PATH to testDb.toByteArray(),
                KEY_PATH to INVALID_KEY_CONTENT.toByteArray()
            )
        )

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
        val fsProvider = newEmptyFsProvider()

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, key)

        // assert
        db.isLeft() shouldBe true
        db.unwrapError() should beInstanceOf<FileNotFoundException>()
    }

    @Test
    fun `createDatabase should read database with composite key`() {
        // arrange
        val key = KeepassKey.CompositeKey(KEY_PATH, DEFAULT_PASSWORD)
        val testDb = createDatabase(COMPOSITE_KEY)
        val fsProvider = MockedFileSystemProvider(
            content = mapOf(
                PATH to testDb.toByteArray(),
                KEY_PATH to COMPOSITE_KEY.binaryData
            )
        )

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, key)

        // assert
        db.isRight() shouldBe true
        db.unwrap().root.isContentEquals(testDb.buildNodeTree()) shouldBe true
    }

    @Test
    fun `createDatabase should return error if password is not correct in composite key`() {
        // arrange
        val key = KeepassKey.CompositeKey(KEY_PATH, INVALID_PASSWORD)
        val testDb = createDatabase(COMPOSITE_KEY)
        val fsProvider = MockedFileSystemProvider(
            content = mapOf(
                PATH to testDb.toByteArray(),
                KEY_PATH to COMPOSITE_KEY.binaryData
            )
        )

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, key)

        // assert
        db.isLeft() shouldBe true
        db.unwrapError() should beInstanceOf<CryptoError.InvalidKey>()
    }

    @Test
    fun `createDatabase should return error if key is missing in composite key`() {
        // arrange
        val key = KeepassKey.CompositeKey(KEY_PATH, DEFAULT_PASSWORD)
        val testDb = createDatabase(COMPOSITE_KEY)
        val fsProvider = MockedFileSystemProvider(
            content = mapOf(
                PATH to testDb.toByteArray()
            )
        )

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, key)

        // assert
        db.isLeft() shouldBe true
        db.unwrapError() should beInstanceOf<FileNotFoundException>()
    }

    @Test
    fun `createDatabase should return error if key is invalid in composite key`() {
        // arrange
        val key = KeepassKey.CompositeKey(KEY_PATH, DEFAULT_PASSWORD)
        val testDb = createDatabase(COMPOSITE_KEY)
        val fsProvider = MockedFileSystemProvider(
            content = mapOf(
                PATH to testDb.toByteArray(),
                KEY_PATH to INVALID_KEY_CONTENT.toByteArray()
            )
        )

        // act
        val db = KotpassDatabaseFactory(fsProvider).createDatabase(PATH, key)

        // assert
        db.isLeft() shouldBe true
        db.unwrapError() should beInstanceOf<CryptoError.InvalidKey>()
    }

    private fun newEmptyFsProvider(): FileSystemProvider =
        MockedFileSystemProvider()

    private fun newFsProviderWithDatabase(
        db: Database<DatabaseElement, KeePassDatabase>
    ): FileSystemProvider =
        MockedFileSystemProvider(
            content = mapOf(
                PATH to db.toByteArray()
            )
        )

    companion object {
        private const val PATH = "db.kdbx"
        private const val KEY_PATH = "key-path"
        private const val INVALID_PASSWORD = "123"
        private const val INVALID_KEY_CONTENT = "invalid-key-content"
    }
}