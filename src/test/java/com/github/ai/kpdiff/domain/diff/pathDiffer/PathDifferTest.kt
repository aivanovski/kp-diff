package com.github.ai.kpdiff.domain.diff.pathDiffer

import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.Named
import com.github.ai.kpdiff.testUtils.NodeTreeDsl.pathTree
import com.github.ai.kpdiff.testUtils.createUuidFrom
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class PathDifferTest {

    @Test
    fun `diff should detect deletion and insertion`() {
        // arrange
        val left = pathTree(Entity("A")) {
            node(Entity("B")) {
                node(Entity("C"))
            }
            node(Entity("D"))
        }

        val right = pathTree(Entity("A")) {
            node(Entity("B"))
            node(Entity("D")) {
                node(Entity("E"))
            }
        }

        // act
        val events = PathDiffer().diff(left, right)

        // assert
        events.size shouldBe 2

        val iterator = events.iterator()
        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<Int>>()
            node.value shouldBe Entity("C")
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<Int>>()
            node.value shouldBe Entity("E")
        }
    }

    @Test
    fun `diff should return empty result`() {
        // arrange
        val left = pathTree(Entity("A")) {
            node(Entity("B"))
            node(Entity("C"))
            node(Entity("D"))
        }

        val right = pathTree(Entity("A")) {
            node(Entity("B"))
            node(Entity("D"))
            node(Entity("C"))
        }

        // act
        val diff = PathDiffer().diff(left, right)

        // assert
        diff.size shouldBe 0
    }

    @Test
    fun `diff should detect update`() {
        // arrange
        val left = pathTree(Entity("A")) {
            node(Entity("B")) {
                node(uid = createUuidFrom("D"), value = Entity("D0"))
            }
            node(Entity("C"))
        }

        val right = pathTree(Entity("A")) {
            node(Entity("B")) {
                node(uid = createUuidFrom("D"), value = Entity("D1"))
            }
            node(Entity("C"))
        }

        // act
        val diff = PathDiffer().diff(left, right)

        // assert
        diff.size shouldBe 1

        val first = diff.first()
        first.shouldBeInstanceOf<DiffEvent.Update<Entity>>()
        first.oldNode.value.name shouldBe "D0"
        first.newNode.value.name shouldBe "D1"
    }

    @Test
    fun `diff should detect uuid change`() {
        // arrange
        val left = pathTree(Entity("A")) {
            node(Entity("B")) {
                node(uid = createUuidFrom("D0"), Entity("D"))
            }
            node(Entity("C"))
        }

        val right = pathTree(Entity("A")) {
            node(Entity("B")) {
                node(uid = createUuidFrom("D1"), Entity("D"))
            }
            node(Entity("C"))
        }

        // act
        val diff = PathDiffer().diff(left, right)

        // assert
        diff.size shouldBe 1

        val first = diff.first()
        first.shouldBeInstanceOf<DiffEvent.Update<Entity>>()

        first.oldNode.uuid shouldBe createUuidFrom("D0")
        first.oldNode.value.name shouldBe "D"

        first.newNode.uuid shouldBe createUuidFrom("D1")
        first.newNode.value.name shouldBe "D"
    }

    @Test
    fun `diff should detect changes for simillar title`() {
        // arrange
        val left = pathTree(Entity("A")) {
            node(Entity("B")) {
                node(uid = createUuidFrom("D0"), Entity("D", value = "D0"))
                node(uid = createUuidFrom("D1"), Entity("D", value = "D1"))
            }
            node(Entity("C"))
        }

        val right = pathTree(Entity("A")) {
            node(Entity("B")) {
                node(uid = createUuidFrom("D0"), Entity("D", value = "D0.1"))
                node(uid = createUuidFrom("D1"), Entity("D", value = "D1.1"))
            }
            node(Entity("C"))
        }

        // act
        val diff = PathDiffer().diff(left, right)

        // assert
        diff.size shouldBe 2

        with(diff[0]) {
            this.shouldBeInstanceOf<DiffEvent.Update<Entity>>()

            oldNode.value shouldBe Entity("D", value = "D0")
            newNode.value shouldBe Entity("D", value = "D0.1")
        }

        with(diff[1]) {
            this.shouldBeInstanceOf<DiffEvent.Update<Entity>>()

            oldNode.value shouldBe Entity("D", value = "D1")
            newNode.value shouldBe Entity("D", value = "D1.1")
        }
    }

    private data class Entity(
        override val name: String,
        val value: String = name
    ) : Named
}