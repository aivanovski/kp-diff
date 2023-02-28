package com.github.ai.kpdiff.domain.input

import com.github.ai.kpdiff.entity.InputReaderType
import com.github.ai.kpdiff.entity.InputReaderType.SECRET
import com.github.ai.kpdiff.entity.InputReaderType.STANDARD

class InputReaderFactory {

    fun createReader(type: InputReaderType): InputReader {
        return when (type) {
            STANDARD -> StandardInputReader()
            SECRET -> SecretInputReader()
        }
    }
}