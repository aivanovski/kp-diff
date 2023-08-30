package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.TestDataFactory.newEntry
import com.github.ai.kpdiff.TestDataFactory.newField
import com.github.ai.kpdiff.TestDataFactory.newGroup
import com.github.ai.kpdiff.domain.diff.differ.PathDatabaseDiffer
import com.github.ai.kpdiff.domain.diff.formatter.DiffDecoratorTest.Ids.ENTRY1
import com.github.ai.kpdiff.domain.diff.formatter.DiffDecoratorTest.Ids.ENTRY2
import com.github.ai.kpdiff.domain.diff.formatter.DiffDecoratorTest.Ids.ENTRY3
import com.github.ai.kpdiff.domain.diff.formatter.DiffDecoratorTest.Ids.ENTRY4
import com.github.ai.kpdiff.domain.diff.formatter.DiffDecoratorTest.Ids.GROUP1
import com.github.ai.kpdiff.domain.diff.formatter.DiffDecoratorTest.Ids.GROUP2
import com.github.ai.kpdiff.domain.diff.formatter.DiffDecoratorTest.Ids.GROUP3
import com.github.ai.kpdiff.domain.diff.formatter.DiffDecoratorTest.Ids.GROUP4
import com.github.ai.kpdiff.domain.diff.formatter.DiffDecoratorTest.Ids.GROUP5
import com.github.ai.kpdiff.domain.diff.formatter.DiffDecoratorTest.Ids.ROOT
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffResult
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.testUtils.AssertionDsl.shouldBe
import com.github.ai.kpdiff.testUtils.NodeTreeDsl.dbTree
import com.github.ai.kpdiff.testUtils.createUuidFrom
import com.github.aivanovski.keepasstreediff.PathDiffer
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DiffDecoratorTest {

    @Test
    fun `aggregate should group events by type`() {
        // arrange
        val lhs = dbTree(newGroup(ROOT)) {
            entry(
                newEntry(
                    id = ENTRY1,
                    custom = mapOf(
                        "A" to "A",
                        "C" to "C"
                    )
                )
            )
        }

        val rhs = dbTree(newGroup(ROOT)) {
            entry(
                newEntry(
                    id = ENTRY1,
                    custom = mapOf(
                        "B" to "B",
                        "C" to "C1"
                    )
                )
            )
        }

        // act
        val events = newDecorator().decorate(lhs.diffWith(rhs))

        // assert
        events.size shouldBe 1

        events[0].first shouldBe createUuidFrom(ENTRY1)
        events[0].second.shouldBe {
            size(3)

            update(
                oldParent = createUuidFrom(ENTRY1),
                newParent = createUuidFrom(ENTRY1),
                oldEntity = newField("C", "C"),
                newEntity = newField("C", "C1")
            )
            delete(createUuidFrom(ENTRY1), newField("A", "A"))
            insert(createUuidFrom(ENTRY1), newField("B", "B"))
        }
    }

    @Test
    fun `aggregate should sort events by entity type`() {
        // arrange
        val lhs = dbTree(newGroup(ROOT)) {
            group(newGroup(GROUP1))
        }

        val rhs = dbTree(newGroup(ROOT)) {
        }

        // act
        val events = newDecorator().decorate(lhs.diffWith(rhs))

        // assert
        events.size shouldBe 1
    }

    @Test
    fun `aggregate should sort events by name`() {
        // arrange
        val lhs = dbTree(newGroup(ROOT)) {
            entry(newEntry(ENTRY1))
        }

        val rhs = dbTree(newGroup(ROOT)) {
            entry(
                newEntry(
                    id = ENTRY1,
                    custom = mapOf(
                        "C" to "C",
                        "A" to "A",
                        "B" to "B"
                    )
                )
            )
        }

        // act
        val events = newDecorator().decorate(lhs.diffWith(rhs))

        // assert
        events.size shouldBe 1
        events.first().first shouldBe createUuidFrom(ENTRY1)
        events.first().second.shouldBe {
            size(3)

            insert(createUuidFrom(ENTRY1), newField("A", "A"))
            insert(createUuidFrom(ENTRY1), newField("B", "B"))
            insert(createUuidFrom(ENTRY1), newField("C", "C"))
        }
    }

    @Test
    fun `aggregate should group events by depth`() {
        // arrange
        val lhs = dbTree(newGroup(ROOT)) {
            entry(newEntry(ENTRY1))
            group(newGroup(GROUP1))

            group(newGroup(GROUP3)) {
                entry(newEntry(ENTRY3))
                group(newGroup(GROUP4))
            }
        }

        val rhs = dbTree(newGroup(ROOT)) {
            entry(newEntry(ENTRY2))
            group(newGroup(GROUP2))

            group(newGroup(GROUP3)) {
                entry(newEntry(ENTRY4))
                group(newGroup(GROUP5))
            }
        }

        // act
        val events = newDecorator().decorate(lhs.diffWith(rhs))

        // assert
        events.size shouldBe 2

        events[0].first shouldBe createUuidFrom(ROOT)
        events[0].second.shouldBe {
            size(4)

            delete(createUuidFrom(ROOT), newGroup(GROUP1))
            delete(createUuidFrom(ROOT), newEntry(ENTRY1))

            insert(createUuidFrom(ROOT), newGroup(GROUP2))
            insert(createUuidFrom(ROOT), newEntry(ENTRY2))
        }

        events[1].first shouldBe createUuidFrom(GROUP3)
        events[1].second.shouldBe {
            size(4)

            delete(createUuidFrom(GROUP3), newGroup(GROUP4))
            delete(createUuidFrom(GROUP3), newEntry(ENTRY3))

            insert(createUuidFrom(GROUP3), newGroup(GROUP5))
            insert(createUuidFrom(GROUP3), newEntry(ENTRY4))
        }
    }

    private fun KeepassDatabase.diffWith(
        another: KeepassDatabase
    ): DiffResult<KeepassDatabase, DatabaseEntity> {
        return PathDatabaseDiffer(PathDiffer()).getDiff(this, another)
    }

    private fun newDecorator(): DiffDecorator =
        DiffDecorator()

    private object Ids {
        const val ROOT = 0
        const val GROUP1 = 1
        const val GROUP2 = 2
        const val GROUP3 = 3
        const val GROUP4 = 4
        const val GROUP5 = 5

        const val ENTRY1 = 11
        const val ENTRY2 = 12
        const val ENTRY3 = 13
        const val ENTRY4 = 14
    }
}