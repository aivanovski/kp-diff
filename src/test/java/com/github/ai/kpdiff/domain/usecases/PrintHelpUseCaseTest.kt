package com.github.ai.kpdiff.domain.usecases

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class PrintHelpUseCaseTest {

    @Test
    fun `shouldPrintHelp should return true if arguments is empty`() {
        PrintHelpUseCase().shouldPrintHelp(
            emptyArray()
        ) shouldBe true
    }

    @Test
    fun `shouldPrintHelp should return true if --help is specified`() {
        PrintHelpUseCase().shouldPrintHelp(
            arrayOf("--help")
        ) shouldBe true
    }

    @Test
    fun `shouldPrintHelp should return true if -h is specified`() {
        PrintHelpUseCase().shouldPrintHelp(
            arrayOf("-h")
        ) shouldBe true
    }

    @Test
    fun `shouldPrintHelp should return false`() {
        PrintHelpUseCase().shouldPrintHelp(
            arrayOf("--option", "help")
        ) shouldBe false
    }
}