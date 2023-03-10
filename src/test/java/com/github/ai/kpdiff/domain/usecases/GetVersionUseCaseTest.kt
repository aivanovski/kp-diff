package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.domain.usecases.GetVersionUseCase.Companion.VERSION_PROPERTY_FILE_NAME
import com.github.ai.kpdiff.testUtils.resourceAsString
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class GetVersionUseCaseTest {

    @Test
    fun getVersionName() {
        // arrange
        val expected = resourceAsString(VERSION_PROPERTY_FILE_NAME).split("=")[1]

        // act
        val version = GetVersionUseCase().getVersionName()

        // assert
        version shouldBe expected
    }
}