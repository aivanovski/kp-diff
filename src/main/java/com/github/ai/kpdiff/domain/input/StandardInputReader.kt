package com.github.ai.kpdiff.domain.input

import com.github.ai.kpdiff.utils.StringUtils.EMPTY

class StandardInputReader : InputReader {
    override fun read(): String {
        return readLine() ?: EMPTY
    }
}