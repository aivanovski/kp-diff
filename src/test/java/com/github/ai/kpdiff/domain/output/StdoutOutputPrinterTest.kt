package com.github.ai.kpdiff.domain.output

import com.github.ai.kpdiff.testUtils.formatStackTrace
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import org.junit.jupiter.api.Test

internal class StdoutOutputPrinterTest {

    @Test
    fun `printLine should print to standard output`() {
        // arrange
        val stream = ByteArrayOutputStream(BUFFER_SIZE)
        System.setOut(PrintStream(stream))

        // act
        StdoutOutputPrinter().printLine(LINE)

        // assert
        stream.toString().trim() shouldBe LINE
    }

    @Test
    fun `printStackTrace should print to standard error output`() {
        // arrange
        val exception = Exception("Test exception")
        val stream = ByteArrayOutputStream(BUFFER_SIZE)
        val expected = formatStackTrace(exception).trim()
        System.setErr(PrintStream(stream))

        // act
        StdoutOutputPrinter().printStackTrace(exception)

        // assert
        stream.toString().trim() shouldBe expected
    }

    companion object {
        private const val BUFFER_SIZE = 2 * 1024
        private const val LINE = "Dummy text fro output"
    }
}