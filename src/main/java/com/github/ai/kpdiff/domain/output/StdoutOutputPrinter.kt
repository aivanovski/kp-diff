package com.github.ai.kpdiff.domain.output

class StdoutOutputPrinter : OutputPrinter {
    override fun printLine(line: String) {
        println(line)
    }

    override fun printStackTrace(exception: Exception) {
        exception.printStackTrace()
    }
}