package com.github.ai.kpdiff.domain.input

import com.github.ai.kpdiff.utils.StringUtils.EMPTY
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class StandardInputReaderTest {

    @Test
    fun `read should read line from standard input`() {
        // arrange
        System.setIn(TEXT.toByteArray().inputStream())

        // act
        val result = StandardInputReader().read()

        // assert
        result shouldBe TEXT
    }

    @Test
    fun `read should return empty string`() {
        // arrange
        System.setIn(byteArrayOf().inputStream())

        // act
        val result = StandardInputReader().read()

        // assert
        result shouldBe EMPTY
    }

    companion object {
        private const val TEXT = "std-input-text"
    }
}