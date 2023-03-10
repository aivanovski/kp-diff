package com.github.ai.kpdiff.testUtils

import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.utils.StringUtils.EMPTY

class CollectingOutputPrinter : OutputPrinter {

    private val lines = mutableListOf<String>()

    override fun printLine(line: String) {
        lines.add(line)
    }

    override fun printStackTrace(exception: Exception) {
        lines.add(formatStackTrace(exception))
    }

    fun getPrintedText(): String {
        return lines.joinToString(separator = EMPTY)
    }
}