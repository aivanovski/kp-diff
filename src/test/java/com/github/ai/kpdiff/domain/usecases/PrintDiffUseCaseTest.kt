package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.TestData
import com.github.ai.kpdiff.TestData.TITLE
import com.github.ai.kpdiff.TestData.UUID1
import com.github.ai.kpdiff.domain.diff.DiffFormatter
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.DiffResult
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.EntryEntity.Companion.PROPERTY_TITLE
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.Node
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.Test

class PrintDiffUseCaseTest {

    private val printer = mockk<OutputPrinter>()
    private val diffFormatter = mockk<DiffFormatter>()

    @Test
    fun `printDiff should pass formatter lines to printer`() {
        // arrange
        val lines = listOf("line1", "line2")
        val options = DiffFormatterOptions()
        val leftDb = mockk<KeepassDatabase>()
        val rightDb = mockk<KeepassDatabase>()
        val events = mockk<List<DiffEvent<DatabaseEntity>>>()
        val diff = DiffResult(leftDb, rightDb, events)
        every { diffFormatter.format(diff, options) }.returns(lines)
        every { printer.printLine(lines[0]) }.returns(Unit)
        every { printer.printLine(lines[1]) }.returns(Unit)

        // act
        PrintDiffUseCase(printer, diffFormatter).printDiff(
            diff = diff,
            options = options
        )

        // assert
        verifySequence {
            diffFormatter.format(diff, options)
            printer.printLine(lines[0])
            printer.printLine(lines[1])
        }
    }
}