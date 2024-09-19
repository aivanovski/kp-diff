package com.github.ai.kpdiff.domain.usecases

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FormatFileSizeUseCaseTest {

    @Test
    fun `formatHumanReadableFileSize should file size correctly`() {
        listOf(
            0L to "0 Bytes",
            1023L to "1023 Bytes",
            1024L to "1 KB",
            12_345L to "12.06 KB",
            10_123_456L to "9.65 MB",
            10_123_456_798L to "9.43 GB",
            1_777_777_777_777_777_777L to "1.54 EB"
        ).forEach { (input, expected) ->
            newUseCase().formatHumanReadableFileSize(input) shouldBe expected
        }
    }

    @Test
    fun `formatHumanReadableFileSize should return error`() {
        shouldThrow<IllegalArgumentException> {
            newUseCase().formatHumanReadableFileSize(-1)
        }
    }

    private fun newUseCase() = FormatFileSizeUseCase()
}