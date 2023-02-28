package com.github.ai.kpdiff.domain.input

import com.github.ai.kpdiff.utils.StringUtils.EMPTY

class SecretInputReader : InputReader {

    override fun read(): String {
        val chars = System.console().readPassword()
        return chars?.let { String(it) } ?: EMPTY
    }
}