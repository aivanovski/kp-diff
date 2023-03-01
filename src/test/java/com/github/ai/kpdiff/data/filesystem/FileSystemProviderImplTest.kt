package com.github.ai.kpdiff.data.filesystem

import com.github.ai.kpdiff.utils.readText
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
        val file = tempDir.resolve(FILE_NAME_TEXT).toFile()
        file.writeText(FILE_CONTENT)

        // act
        val content = FileSystemProviderImpl().open(file.path)

        // assert
        content.isRight() shouldBe true
        content.unwrap().readText() shouldBe FILE_CONTENT
    }

    @Test
    fun `open should return FileNotFoundException`() {
        // arrange
        val file = tempDir.resolve(FILE_NAME_IMAGE).toFile()

        // act
        val content = FileSystemProviderImpl().open(file.path)

        // assert
        content.isLeft() shouldBe true
        content.unwrapError() should beInstanceOf<FileNotFoundException>()
    }

    companion object {
        private const val FILE_NAME_TEXT = "text.txt"
        private const val FILE_NAME_IMAGE = "image.jpg"
        private const val FILE_CONTENT = "Test file content"
    }
}