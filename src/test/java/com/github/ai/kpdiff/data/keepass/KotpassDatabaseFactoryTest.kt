package com.github.ai.kpdiff.data.keepass

import com.github.ai.kpdiff.TestData
import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.utils.contentStream
import com.github.ai.kpdiff.utils.convert
import com.github.ai.kpdiff.utils.isContentEquals
import io.github.anvell.kotpass.errors.CryptoError
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.kotest.matchers.types.beTheSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import java.io.FileNotFoundException
import org.junit.jupiter.api.Test

internal class KotpassDatabaseFactoryTest {

    @Test
    fun `createDatabase should read database with password`() {
        // arrange
        val provider = mockk<FileSystemProvider>()
        val testDb = TestData.DB_WITH_PASSWORD
        val key = testDb.key.convert()
        every { provider.open(PATH) }.returns(Either.Right(testDb.contentStream()))

        // act
        val db = KotpassDatabaseFactory(provider).createDatabase(PATH, key)

        // assert
        db.isRight() shouldBe true
        db.unwrap().isContentEquals(testDb) shouldBe true
    }

    @Test
    fun `createDatabase should return error if password is not correct`() {
        // arrange
        val provider = mockk<FileSystemProvider>()
        val testDb = TestData.DB_WITH_PASSWORD
        val key = KeepassKey.PasswordKey(INVALID_PASSWORD)
        every { provider.open(PATH) }.returns(Either.Right(testDb.contentStream()))

        // act
        val db = KotpassDatabaseFactory(provider).createDatabase(PATH, key)

        // assert
        db.isLeft() shouldBe true
        db.unwrapError() should beInstanceOf<CryptoError.InvalidKey>()
    }

    @Test
    fun `createDatabase should return error if db file is not exist`() {
        // arrange
        val provider = mockk<FileSystemProvider>()
        val testDb = TestData.DB_WITH_PASSWORD
        val key = testDb.key.convert()
        val expectedException = FileNotFoundException()
        every { provider.open(PATH) }.returns(Either.Left(expectedException))

        // act
        val db = KotpassDatabaseFactory(provider).createDatabase(PATH, key)

        // assert
        db.isLeft() shouldBe true
        db.unwrapError() should beTheSameInstanceAs(expectedException)
    }

    companion object {
        private const val PATH = "db.kdbx"
        private const val INVALID_PASSWORD = "123"
    }
}