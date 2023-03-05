package com.github.ai.kpdiff.domain.output

class StdoutOutputWriter : OutputWriter {
    override fun printLine(line: String) {
        println(line)
    }

    override fun printStackTrace(exception: Exception) {
        exception.printStackTrace()
    }
}