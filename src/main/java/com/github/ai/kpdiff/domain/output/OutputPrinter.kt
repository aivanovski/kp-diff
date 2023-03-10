package com.github.ai.kpdiff.domain.output

interface OutputPrinter {
    fun printLine(line: String)
    fun printStackTrace(exception: Exception)
}