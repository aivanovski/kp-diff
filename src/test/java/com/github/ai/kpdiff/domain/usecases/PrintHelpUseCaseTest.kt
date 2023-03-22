package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.TestData.VERSION
import com.github.ai.kpdiff.domain.argument.OptionalArgument
import com.github.ai.kpdiff.domain.usecases.PrintHelpUseCase.Companion.HELP_TEXT
import com.github.ai.kpdiff.testUtils.CollectingOutputPrinter
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class PrintHelpUseCaseTest {

    @Test
    fun `shouldPrintHelp should return true if arguments is empty`() {
        newUseCase().shouldPrintHelp(
            emptyArray()
        ) shouldBe true
    }

    @Test
    fun `shouldPrintHelp should return true if --help is specified`() {
        newUseCase().shouldPrintHelp(
            arrayOf(OptionalArgument.HELP.cliFullName)
        ) shouldBe true
    }

    @Test
    fun `shouldPrintHelp should return true if -h is specified`() {
        newUseCase().shouldPrintHelp(
            arrayOf(OptionalArgument.HELP.cliShortName)
        ) shouldBe true
    }

    @Test
    fun `shouldPrintHelp should return false`() {
        newUseCase().shouldPrintHelp(
            arrayOf("--option", "help")
        ) shouldBe false
    }

    @Test
    fun `printHelp should print help text`() {
        // arrange
        val versionUseCase = mockk<GetVersionUseCase>()
        val printer = CollectingOutputPrinter()
        every { versionUseCase.getVersionName() }.returns(VERSION)

        // act
        newUseCase(versionUseCase).printHelp(printer)

        // assert
        printer.getPrintedText() shouldBe String.format(HELP_TEXT, VERSION)
    }

    private fun newUseCase(
        getVersionUseCase: GetVersionUseCase = mockk()
    ): PrintHelpUseCase {
        return PrintHelpUseCase(
            getVersionUseCase = getVersionUseCase
        )
    }
}