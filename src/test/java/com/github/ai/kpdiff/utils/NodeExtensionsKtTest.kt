package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.entity.Node
import com.github.ai.kpdiff.testUtils.NodeTreeDsl.tree
import com.github.ai.kpdiff.testUtils.createUuidFrom
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class NodeExtensionsKtTest {

    @Test
    fun `traverseByValueType should return all nodes`() {
        // act
        val values = TREE.traverseByValueType(String::class)
            .map { it.value }

        // assert
        values shouldBe TREE_NODES_BFS
    }

    @Test
    fun `traverseAllByType should filter node by value type`() {
        // arrange
        val tree = Node(
            uuid = createUuidFrom("A"),
            value = "A",
            nodes = mutableListOf<Node<Any>>(
                Node(
                    uuid = createUuidFrom("B"),
                    value = "B",
                    nodes = mutableListOf()
                ),
                Node(
                    uuid = createUuidFrom(1),
                    value = 1,
                    nodes = mutableListOf()
                )
            )
        )

        // act
        val values = tree.traverseByValueType(String::class)
            .map { it.value }

        // assert
        values shouldBe listOf("A", "B")
    }

    @Test
    fun `traverseWithParents should return parent and node pair`() {
        // act
        val pairs = TREE.traverseWithParents()
            .map { (parentNode, node) -> Pair(parentNode?.value, node.value) }

        // assert
        pairs shouldBe listOf(
            Pair(null, "A"),
            Pair("A", "B"),
            Pair("A", "C"),
            Pair("B", "D"),
            Pair("B", "E"),
            Pair("C", "F"),
            Pair("C", "G")
        )
    }

    @Test
    fun `traverse should return all nodes from tree`() {
        // act
        val nodes = TREE.traverse()
            .map { node -> node.value }

        // assert
        nodes shouldBe TREE_NODES_BFS
    }

    @Test
    fun `buildDepthMap should map uuid to depth`() {
        // arrange

        // act
        val depthMap = TREE.buildDepthMap()

        // assert
        depthMap shouldBe mapOf(
            createUuidFrom("A") to 0,

            createUuidFrom("B") to 1,
            createUuidFrom("C") to 1,

            createUuidFrom("D") to 2,
            createUuidFrom("E") to 2,
            createUuidFrom("F") to 2,
            createUuidFrom("G") to 2
        )
    }

    companion object {
        private val TREE = tree("A") {
            node("B") {
                node("D")
                node("E")
            }
            node("C") {
                node("F")
                node("G")
            }
        }
        private val TREE_NODES_BFS = listOf("A", "B", "C", "D", "E", "F", "G")
    }
}