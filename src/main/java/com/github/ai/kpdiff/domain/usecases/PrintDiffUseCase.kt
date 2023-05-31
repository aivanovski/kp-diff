package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.domain.diff.DiffFormatter
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.DiffResult
import com.github.ai.kpdiff.entity.KeepassDatabase

class PrintDiffUseCase(
    private val printer: OutputPrinter,
    private val diffFormatter: DiffFormatter
) {

    fun printDiff(
        diff: DiffResult<KeepassDatabase, DatabaseEntity>,
        options: DiffFormatterOptions
    ) {
        val lines = diffFormatter.format(diff, options)
        lines.forEach { printer.printLine(it) }
    }
}