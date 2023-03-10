package com.github.ai.kpdiff.domain

import com.github.ai.kpdiff.TestData.EXCEPTION_MESSAGE
import com.github.ai.kpdiff.domain.Strings.ERROR_HAS_BEEN_OCCURRED
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.testUtils.CollectingOutputPrinter
import com.github.ai.kpdiff.testUtils.formatStackTrace
import com.github.ai.kpdiff.utils.StringUtils.EMPTY
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class ErrorHandlerTest {

    @Test
    fun `handleIfLeft should print message if it is not empty`() {
        // arrange
        val printer = CollectingOutputPrinter()
        val exception = Exception(EXCEPTION_MESSAGE)
        val message = String.format(
            ERROR_HAS_BEEN_OCCURRED,
            EXCEPTION_MESSAGE
        )

        // act
        ErrorHandler(printer).handleIfLeft(Either.Left(exception))

        // assert
        printer.getPrintedText() shouldBe message
    }

    @Test
    fun `handleIfLeft should print stack trace if message is null`() {
        // arrange
        val printer = CollectingOutputPrinter()
        val exception = Exception()
        val stacktrace = formatStackTrace(exception)

        // act
        ErrorHandler(printer).handleIfLeft(Either.Left(exception))

        // assert
        printer.getPrintedText() shouldBe stacktrace
    }

    @Test
    fun `handleIfLeft should print stack trace if message is empty`() {
        // arrange
        val printer = CollectingOutputPrinter()
        val exception = Exception(EMPTY)
        val stacktrace = formatStackTrace(exception)

        // act
        ErrorHandler(printer).handleIfLeft(Either.Left(exception))

        // assert
        printer.getPrintedText() shouldBe stacktrace
    }

    @Test
    fun `handleIfLeft should not print error`() {
        // arrange
        val printer = CollectingOutputPrinter()

        // act
        ErrorHandler(printer).handleIfLeft(Either.Right(Unit))

        // assert
        printer.getPrintedText().isEmpty() shouldBe true
    }
}