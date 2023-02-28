package com.github.ai.kpdiff.domain.input

import com.github.ai.kpdiff.entity.InputReaderType.SECRET
import com.github.ai.kpdiff.entity.InputReaderType.STANDARD
import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
import org.junit.jupiter.api.Test

internal class InputReaderFactoryTest {

    @Test
    fun `createReader should create StandardInputReader`() {
        val reader = InputReaderFactory().createReader(STANDARD)
        reader should beInstanceOf<StandardInputReader>()
    }

    @Test
    fun `createReader should create SecretInputReader`() {
        val reader = InputReaderFactory().createReader(SECRET)
        reader should beInstanceOf<SecretInputReader>()
    }
}