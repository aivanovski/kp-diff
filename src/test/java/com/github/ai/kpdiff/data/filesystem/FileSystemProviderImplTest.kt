package com.github.ai.kpdiff.data.filesystem

import com.github.ai.kpdiff.TestData.FILE_CONTENT
import com.github.ai.kpdiff.TestData.FILE_PATH
import com.github.ai.kpdiff.TestData.PARENT_PATH
import com.github.ai.kpdiff.TestData.UPDATED_FILE_CONTENT
import com.github.ai.kpdiff.domain.Strings.UNABLE_TO_CREATE_DIRECTORY
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.testUtils.MockedFileFactory
import com.github.ai.kpdiff.testUtils.ThrowOnReadInputStream
import com.github.ai.kpdiff.testUtils.readText
import com.github.ai.kpdiff.testUtils.toInputStream
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Path
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class FileSystemProviderImplTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `openForRead should return input stream`() {
        // arrange
        val file = newFile()
        file.writeText(FILE_CONTENT)
        file.exists() shouldBe true

        // act
        val content = newFsProvider().openForRead(file.path)

        // assert
        content.isRight() shouldBe true
        content.unwrap().readText() shouldBe FILE_CONTENT
    }

    @Test
    fun `openForRead should return FileNotFoundException`() {
        // arrange
        val file = newFile()
        file.exists() shouldBe false

        // act
        val content = newFsProvider().openForRead(file.path)

        // assert
        content.isLeft() shouldBe true
        content.unwrapError() should beInstanceOf<FileNotFoundException>()
    }

    @Test
    fun `openForWrite should create new file`() {
        // arrange
        val file = newFile()
        file.exists() shouldBe false

        // act
        val result = newFsProvider().write(
            path = file.path,
            content = FILE_CONTENT.toInputStream()
        )

        // assert
        result shouldBe Either.Right(Unit)
        file.exists() shouldBe true
        file.readText() shouldBe FILE_CONTENT
    }

    @Test
    fun `openForWrite should overwrite existing file`() {
        // arrange
        val file = newFile()
        file.writeText(FILE_CONTENT)
        file.readText() shouldBe FILE_CONTENT

        // act
        val result = newFsProvider().write(
            path = file.path,
            content = UPDATED_FILE_CONTENT.toInputStream()
        )

        // assert
        result shouldBe Either.Right(Unit)
        file.exists() shouldBe true
        file.readText() shouldBe UPDATED_FILE_CONTENT
    }

    @Test
    fun `openForWrite should create not existing directories in path`() {
        // arrange
        val file = tempDir.resolve("subdi1/subdir2/$FILE_NAME").toFile()
        val parent = file.parentFile
        parent.exists() shouldBe false

        // act
        val result = newFsProvider().write(
            path = file.path,
            content = FILE_CONTENT.toInputStream()
        )

        // assert
        result shouldBe Either.Right(Unit)
        file.exists() shouldBe true
        file.readText() shouldBe FILE_CONTENT
    }

    @Test
    fun `exists should return true`() {
        // arrange
        val file = newFile()
        file.writeText(FILE_CONTENT)
        file.exists() shouldBe true

        // act
        val isExist = newFsProvider().exists(file.path)

        // assert
        isExist shouldBe true
    }

    @Test
    fun `exists should return false`() {
        // arrange
        val file = newFile()
        file.exists() shouldBe false

        // act
        val isExist = newFsProvider().exists(file.path)

        // assert
        isExist shouldBe false
    }

    @Test
    fun `write should return error if unable to create directory`() {
        // arrange
        val fileFactory = mockk<FileFactory>()
        val file = mockk<File>()
        val parentFile = mockk<File>()

        every { fileFactory.newFile(FILE_PATH) }.returns(file)
        every { file.parentFile }.returns(parentFile)
        every { parentFile.path }.returns(PARENT_PATH)
        every { parentFile.exists() }.returns(false)
        every { parentFile.mkdirs() }.returns(false)

        // act
        val result = newFsProvider(fileFactory).write(
            path = FILE_PATH,
            content = FILE_CONTENT.toInputStream()
        )

        // assert
        result.isLeft() shouldBe true
        result.unwrapError() should beInstanceOf<IOException>()
        result.unwrapError().message shouldBe UNABLE_TO_CREATE_DIRECTORY.format(PARENT_PATH)
    }

    @Test
    fun `write should return error if IOException occurs during writing`() {
        // arrange
        val file = newFile()
        val exception = IOException()
        val content = ThrowOnReadInputStream(exception = exception)

        // act
        val result = newFsProvider().write(
            path = file.path,
            content = content
        )

        // assert
        result shouldBe Either.Left(exception)
    }

    @Test
    fun `getName should return file name`() {
        // arrange
        val file = newFile()
        file.writeText(FILE_CONTENT)
        file.exists() shouldBe true

        // act
        val result = newFsProvider().getName(
            path = file.path
        )

        // assert
        result shouldBe Either.Right(file.name)
    }

    @Test
    fun `getName should return FileNotFoundException`() {
        // arrange
        val file = newFile()
        file.exists() shouldBe false

        // act
        val content = newFsProvider().getName(file.path)

        // assert
        content.isLeft() shouldBe true
        content.unwrapError() should beInstanceOf<FileNotFoundException>()
    }

    private fun newFile(): File {
        return tempDir.resolve(FILE_NAME).toFile()
    }

    private fun newFsProvider(fileFactory: FileFactory = MockedFileFactory()): FileSystemProvider {
        return FileSystemProviderImpl(
            fileFactory = fileFactory
        )
    }

    companion object {
        private const val FILE_NAME = "text.txt"
    }
}