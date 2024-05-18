package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.DatabaseFactory.createDatabase
import com.github.ai.kpdiff.testUtils.buildNodeTree
import com.github.ai.kpdiff.testUtils.isContentEquals
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class KotpassExtensionsKtTest {

    // TODO: check this test

    @Test
    fun `toTree should build a valid tree`() {
        // arrange
        val testDb = createDatabase()
        val root = testDb.underlying.content.group

        // act
        val tree = root.buildNodeTree()

        // assert
        tree.isContentEquals(testDb.buildNodeTree()) shouldBe true
    }
}