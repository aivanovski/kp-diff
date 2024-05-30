package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.TestData.VERSION
import com.github.ai.kpdiff.domain.usecases.PrintVersionUseCase.Companion.TEXT
import com.github.ai.kpdiff.testUtils.CollectingOutputPrinter
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class PrintVersionUseCaseTest {

    @Test
    fun `printVersion should print version`() {
        // arrange
        val getVersionUseCase = mockk<GetVersionUseCase>()
        val printer = CollectingOutputPrinter()
        every { getVersionUseCase.getVersionName() }.returns(VERSION)

        // act
        newUseCase(getVersionUseCase).printVersion(printer)

        // assert
        printer.getPrintedText() shouldBe String.format(TEXT, VERSION)
    }

    private fun newUseCase(getVersionUseCase: GetVersionUseCase = mockk()): PrintVersionUseCase {
        return PrintVersionUseCase(getVersionUseCase)
    }
}