package com.github.ai.kpdiff.domain

import com.github.ai.kpdiff.domain.Errors.ERROR_HAS_BEEN_OCCURRED
import com.github.ai.kpdiff.domain.output.OutputWriter
import com.github.ai.kpdiff.entity.Either
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.jupiter.api.Test

internal class ErrorHandlerTest {

    @Test
    fun `handleIfLeft should print error`() {
        // arrange
        val writer = mockk<OutputWriter>()
        val exception = Exception("Test exception")
        val message = String.format(
            ERROR_HAS_BEEN_OCCURRED,
            exception.toString()
        )
        every { writer.printLine(message) }.returns(Unit)
        every { writer.printStackTrace(exception) }.returns(Unit)

        // act
        ErrorHandler(writer).handleIfLeft(Either.Left(exception))

        // assert
        verifySequence {
            writer.printLine(message)
            writer.printStackTrace(exception)
        }
    }

    @Test
    fun `hadleIfLeft should not print error`() {
        // arrange
        val writer = mockk<OutputWriter>()

        // act
        ErrorHandler(writer).handleIfLeft(Either.Right(Unit))

        // assert
        confirmVerified(writer)
    }
}