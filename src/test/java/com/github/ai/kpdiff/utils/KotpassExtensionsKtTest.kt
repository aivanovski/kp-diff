package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.TestData
import com.github.ai.kpdiff.testUtils.isContentEquals
import com.github.ai.kpdiff.testUtils.open
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class KotpassExtensionsKtTest {

    @Test
    fun `toTree should build a valid tree`() {
        // arrange
        val testDb = TestData.DB_WITH_PASSWORD
        val root = testDb.open().content.group

        // act
        val tree = root.buildNodeTree()

        // assert
        tree.isContentEquals(testDb) shouldBe true
    }
}