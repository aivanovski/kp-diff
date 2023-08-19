package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.entity.InputReaderType

class DetermineInputTypeUseCase {

    fun getInputReaderType(): InputReaderType {
        return if (System.console() != null) {
            InputReaderType.SECRET
        } else {
            InputReaderType.STANDARD
        }
    }
}