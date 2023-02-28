package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.domain.input.InputReader
import com.github.ai.kpdiff.domain.input.InputReaderFactory
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.InputReaderType.STANDARD
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.jupiter.api.Test

internal class ReadPasswordUseCaseTest {

    @Test
    fun `readPassword should read password`() {
        // arrange
        val factory = mockk<InputReaderFactory>()
        val reader = mockk<InputReader>()
        every { factory.createReader(STANDARD) }.returns(reader)
        every { reader.read() }.returns(PASSWORD)

        // act
        val result = ReadPasswordUseCase(factory).readPassword(STANDARD)

        // assert
        verifySequence {
            factory.createReader(STANDARD)
            reader.read()
        }
        result shouldBe Either.Right(PASSWORD)
    }

    companion object {
        private val PASSWORD = "password"
    }
}