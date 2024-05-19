package com.github.ai.kpdiff.data.filesystem

import io.kotest.matchers.shouldBe
import java.io.File
import org.junit.jupiter.api.Test

class FileFactoryImplTest {

    private val fileFactory = FileFactoryImpl()

    @Test
    fun `newFile should not modify an absolute path`() {
        listOf(
            "" to File(""),
            "test.txt" to File("./test.txt"),
            "/test.txt" to File("/test.txt"),
            "./test.txt" to File("./test.txt")
        ).forEach { (path, expectedFile) ->
            fileFactory.newFile(path).path shouldBe expectedFile.path
        }
    }
}