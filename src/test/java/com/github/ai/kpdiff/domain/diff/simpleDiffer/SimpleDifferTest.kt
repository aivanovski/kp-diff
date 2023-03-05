package com.github.ai.kpdiff.domain.diff.simpleDiffer

import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.testUtils.NodeTreeDsl.tree
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

internal class SimpleDifferTest {

    @Test
    fun `diff should detect deletion and insertion`() {
        // arrange
        val left = tree("A") {
            node("B") {
                node("C")
            }
            node("D")
        }

        val right = tree("A") {
            node("B")
            node("D") {
                node("E")
            }
        }

        // act
        val diff = SimpleDiffer().diff(left, right)

        // assert
        diff.size shouldBe 2

        val first = diff[0]
        first.shouldBeInstanceOf<DiffEvent.Delete<Int>>()
        first.node.value shouldBe "C"

        val second = diff[1]
        second.shouldBeInstanceOf<DiffEvent.Insert<Int>>()
        second.node.value shouldBe "E"
    }

    @Test
    fun `diff should return empty result`() {
        // arrange
        val left = tree("A") {
            node("B")
            node("C")
            node("D")
        }

        val right = tree("A") {
            node("B")
            node("D")
            node("C")
        }

        // act
        val diff = SimpleDiffer().diff(left, right)

        // assert
        diff.size shouldBe 0
    }

    @Test
    fun `diff should detect change`() {
        // arrange
        val left = tree("A") {
            node("B") {
                node("D", value = "D0")
            }
            node("C")
        }

        val right = tree("A") {
            node("B") {
                node("D", value = "D1")
            }
            node("C")
        }

        // act
        val diff = SimpleDiffer().diff(left, right)

        // assert
        diff.size shouldBe 1

        val first = diff.first()
        first.shouldBeInstanceOf<DiffEvent.Update<Int>>()
        first.oldNode.value shouldBe "D0"
        first.newNode.value shouldBe "D1"
    }
}