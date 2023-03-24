package com.github.ai.kpdiff.domain.diff

class TerminalOutputFormatter {

    fun format(line: String, color: Color): String {
        return if (color != Color.NONE) {
            "${color.value}$line${Color.DEFAULT.value}"
        } else {
            line
        }
    }

    enum class Color(val value: String) {
        NONE(""),
        DEFAULT("\u001B[0m"),
        YELLOW("\u001B[33m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m")
    }
}