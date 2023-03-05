package com.github.ai.kpdiff.domain.output

interface OutputWriter {
    fun printLine(line: String)
    fun printStackTrace(exception: Exception)
}