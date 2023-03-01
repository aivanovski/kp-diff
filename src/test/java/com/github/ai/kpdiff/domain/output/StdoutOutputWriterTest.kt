package com.github.ai.kpdiff.domain.output

import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import org.junit.jupiter.api.Test

internal class StdoutOutputWriterTest {

    @Test
    fun `printLine should print to standard output`() {
        // arrange
        val stream = ByteArrayOutputStream(BUFFER_SIZE)
        System.setOut(PrintStream(stream))

        // act
        StdoutOutputWriter().printLine(LINE)

        // assert
        stream.toString().trim() shouldBe LINE
    }

    @Test
    fun `printStackTrace should print to standard error output`() {
        // arrange
        val exception = Exception("Text exception")
        val stream = ByteArrayOutputStream(BUFFER_SIZE)
        val expected = getStackTrace(exception).trim()
        System.setErr(PrintStream(stream))

        // act
        StdoutOutputWriter().printStackTrace(exception)

        // assert
        stream.toString().trim() shouldBe expected
    }

    private fun getStackTrace(exception: Exception): String {
        val stream = ByteArrayOutputStream(4096)
        exception.printStackTrace(PrintStream(stream))
        return stream.toString()
    }

    companion object {
        private const val BUFFER_SIZE = 2 * 1024
        private const val LINE = "Dummy text fro output"
    }
}