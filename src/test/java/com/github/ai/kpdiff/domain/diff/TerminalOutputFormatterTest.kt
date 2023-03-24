package com.github.ai.kpdiff.domain.diff

import com.github.ai.kpdiff.domain.diff.TerminalOutputFormatter.Color
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class TerminalOutputFormatterTest {

    @Test
    fun `format should return colored output`() {
        newFormatter().format(
            TEXT,
            Color.DEFAULT
        ) shouldBe "${Color.DEFAULT.value}$TEXT${Color.DEFAULT.value}"

        newFormatter().format(
            TEXT,
            Color.RED
        ) shouldBe "${Color.RED.value}$TEXT${Color.DEFAULT.value}"
    }

    @Test
    fun `format should not modify line`() {
        newFormatter().format(TEXT, Color.NONE) shouldBe TEXT
    }

    private fun newFormatter(): TerminalOutputFormatter = TerminalOutputFormatter()

    companion object {
        private const val TEXT = "text"
    }
}