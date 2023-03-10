package com.github.ai.kpdiff.data.filesystem

import com.github.ai.kpdiff.TestData.FILE_CONTENT
import com.github.ai.kpdiff.testUtils.readText
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import java.io.FileNotFoundException
import java.nio.file.Path
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class FileSystemProviderImplTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `open should return input stream`() {
        // arrange
        val file = tempDir.resolve(FILE_NAME).toFile()
        file.writeText(FILE_CONTENT)
        file.exists() shouldBe true

        // act
        val content = FileSystemProviderImpl().open(file.path)

        // assert
        content.isRight() shouldBe true
        content.unwrap().readText() shouldBe FILE_CONTENT
    }

    @Test
    fun `open should return FileNotFoundException`() {
        // arrange
        val file = tempDir.resolve(FILE_NAME).toFile()
        file.exists() shouldBe false

        // act
        val content = FileSystemProviderImpl().open(file.path)

        // assert
        content.isLeft() shouldBe true
        content.unwrapError() should beInstanceOf<FileNotFoundException>()
    }

    @Test
    fun `exists should return true`() {
        // arrange
        val file = tempDir.resolve(FILE_NAME).toFile()
        file.writeText(FILE_CONTENT)
        file.exists() shouldBe true

        // act
        val isExist = FileSystemProviderImpl().exists(file.path)

        // assert
        isExist shouldBe true
    }

    @Test
    fun `exists should return false`() {
        // arrange
        val file = tempDir.resolve(FILE_NAME).toFile()
        file.exists() shouldBe false

        // act
        val isExist = FileSystemProviderImpl().exists(file.path)

        // assert
        isExist shouldBe false
    }

    companion object {
        private const val FILE_NAME = "text.txt"
    }
}