package com.github.ai.kpdiff.domain.diff.pathDiffer

import com.github.ai.kpdiff.TestData.DB_WITH_PASSWORD
import com.github.ai.kpdiff.TestData.DB_WITH_PASSWORD_MODIFIED
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.EntryEntity.Companion.PROPERTY_NOTES
import com.github.ai.kpdiff.entity.EntryEntity.Companion.PROPERTY_PASSWORD
import com.github.ai.kpdiff.entity.EntryEntity.Companion.PROPERTY_TITLE
import com.github.ai.kpdiff.entity.EntryEntity.Companion.PROPERTY_URL
import com.github.ai.kpdiff.entity.EntryEntity.Companion.PROPERTY_USERNAME
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.testUtils.NodeTreeDsl.dbTree
import com.github.ai.kpdiff.testUtils.createUuidFrom
import com.github.ai.kpdiff.testUtils.open
import com.github.ai.kpdiff.testUtils.sortForAssertion
import com.github.ai.kpdiff.utils.StringUtils.EMPTY
import com.github.ai.kpdiff.utils.buildNodeTree
import com.github.ai.kpdiff.utils.getTitle
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.UUID
import org.junit.jupiter.api.Test

internal class PathDatabaseDifferTest {

    @Test
    fun `diff should work with real database`() {
        // arrange
        val lhs = KeepassDatabase(
            root = DB_WITH_PASSWORD.open().content.group.buildNodeTree()
        )
        val rhs = KeepassDatabase(
            root = DB_WITH_PASSWORD_MODIFIED.open().content.group.buildNodeTree()
        )

        // act
        val diff = PathDatabaseDiffer().getDiff(lhs, rhs)

        // assert
        diff.lhs shouldBe lhs
        diff.rhs shouldBe rhs

        val events = diff.events.sortForAssertion()
        events.size shouldBe 5

        val iterator = events.iterator()
        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<GroupEntity>>()
            node.value.name shouldBe "Inner group 2"
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<GroupEntity>>()
            node.value.name shouldBe "Inner group 3"
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<EntryEntity>>()
            node.value.getTitle() shouldBe "Entry 3"
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<EntryEntity>>()
            node.value.getTitle() shouldBe "Entry 5"
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Update<FieldEntity>>()
            oldNode.value.name shouldBe "Title"
            oldNode.value.value shouldBe "Entry 4"
            newNode.value.value shouldBe "Entry 4 modified"
        }
    }

    @Test
    fun `diff should detect entry insertion`() {
        // arrange
        val lhs = dbTree(ROOT) {
            entry(ENTRY_1)
        }
        val rhs = dbTree(ROOT) {
            entry(ENTRY_1)
            entry(ENTRY_2)
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 1

        with(events.first()) {
            shouldBeInstanceOf<DiffEvent.Insert<EntryEntity>>()
            node.value shouldBe ENTRY_2
        }
    }

    @Test
    fun `diff should detect entry deletion`() {
        // arrange
        val lhs = dbTree(ROOT) {
            entry(ENTRY_1)
            entry(ENTRY_2)
        }
        val rhs = dbTree(ROOT) {
            entry(ENTRY_1)
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 1

        with(events.first()) {
            shouldBeInstanceOf<DiffEvent.Delete<EntryEntity>>()
            node.value shouldBe ENTRY_2
        }
    }

    @Test
    fun `diff should detect entry update`() {
        // arrange
        val lhs = dbTree(ROOT) {
            entry(ENTRY_1)
        }
        val rhs = dbTree(ROOT) {
            entry(ENTRY_1_MODIFIED)
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 1

        with(events.first()) {
            shouldBeInstanceOf<DiffEvent.Update<FieldEntity>>()

            oldNode.value.name shouldBe PROPERTY_USERNAME
            oldNode.value.value shouldBe ENTRY_1.getUsername()

            newNode.value.name shouldBe PROPERTY_USERNAME
            newNode.value.value shouldBe ENTRY_1_MODIFIED.getUsername()
        }
    }

    @Test
    fun `diff should detect entry insertion and deletion`() {
        // arrange
        val lhs = dbTree(ROOT) {
            group(GROUP_A) {
                entry(ENTRY_1)
            }
            group(GROUP_B)
        }
        val rhs = dbTree(ROOT) {
            group(GROUP_A)
            group(GROUP_B) {
                entry(ENTRY_1)
            }
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 2

        val iterator = events.iterator()
        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<EntryEntity>>()
            node.value shouldBe ENTRY_1
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<EntryEntity>>()
            node.value shouldBe ENTRY_1
        }
    }

    @Test
    fun `diff should detect field insertion`() {
        // arrange
        val modifiedEntry = ENTRY_1.modify(
            custom = mapOf(
                CUSTOM_PROPERTY_NAME to CUSTOM_PROPERTY_VALUE
            )
        )
        val lhs = dbTree(ROOT) {
            entry(ENTRY_1)
        }
        val rhs = dbTree(ROOT) {
            entry(modifiedEntry)
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 1

        with(events.first()) {
            shouldBeInstanceOf<DiffEvent.Insert<FieldEntity>>()
            node.value.name shouldBe CUSTOM_PROPERTY_NAME
            node.value.value shouldBe CUSTOM_PROPERTY_VALUE
        }
    }

    @Test
    fun `diff should detect field deletion`() {
        // arrange
        val modifiedEntry = ENTRY_1.modify(
            custom = mapOf(
                CUSTOM_PROPERTY_NAME to CUSTOM_PROPERTY_VALUE
            )
        )
        val lhs = dbTree(ROOT) {
            entry(modifiedEntry)
        }
        val rhs = dbTree(ROOT) {
            entry(ENTRY_1)
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 1

        with(events.first()) {
            shouldBeInstanceOf<DiffEvent.Delete<FieldEntity>>()
            node.value.name shouldBe CUSTOM_PROPERTY_NAME
            node.value.value shouldBe CUSTOM_PROPERTY_VALUE
        }
    }

    @Test
    fun `diff should detect field update`() {
        // arrange
        val modifiedEntry = ENTRY_1.modify(
            username = "modified-username"
        )
        val lhs = dbTree(ROOT) {
            entry(ENTRY_1)
        }
        val rhs = dbTree(ROOT) {
            entry(modifiedEntry)
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 1

        with(events.first()) {
            shouldBeInstanceOf<DiffEvent.Update<FieldEntity>>()

            oldNode.value.name shouldBe PROPERTY_USERNAME
            oldNode.value.value shouldBe ENTRY_1.getUsername()

            newNode.value.name shouldBe PROPERTY_USERNAME
            newNode.value.value shouldBe modifiedEntry.getUsername()
        }
    }

    @Test
    fun `diff should detect group insertion`() {
        // arrange
        val lhs = dbTree(ROOT)
        val rhs = dbTree(ROOT) {
            group(GROUP_A)
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 1

        with(events.first()) {
            shouldBeInstanceOf<DiffEvent.Insert<GroupEntity>>()
            node.value shouldBe GROUP_A
        }
    }

    @Test
    fun `diff should detect group deletion`() {
        // arrange
        val lhs = dbTree(ROOT) {
            group(GROUP_A)
        }
        val rhs = dbTree(ROOT) {
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 1

        with(events.first()) {
            shouldBeInstanceOf<DiffEvent.Delete<GroupEntity>>()
            node.value shouldBe GROUP_A
        }
    }

    @Test
    fun `diff should detect group insertion and deletion`() {
        // arrange
        val lhs = dbTree(ROOT) {
            group(GROUP_A) {
                group(GROUP_C)
            }
            group(GROUP_B)
        }
        val rhs = dbTree(ROOT) {
            group(GROUP_A)
            group(GROUP_B) {
                group(GROUP_C)
            }
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 2

        val iterator = events.iterator()
        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<GroupEntity>>()
            node.value shouldBe GROUP_C
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<GroupEntity>>()
            node.value shouldBe GROUP_C
        }
    }

    @Test
    fun `diff should include entries inside group`() {
        // arrange
        val lhs = dbTree(ROOT) {
            group(GROUP_A) {
                entry(ENTRY_1)
            }
        }
        val rhs = dbTree(ROOT) {
            group(GROUP_B) {
                entry(ENTRY_2)
            }
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 4

        val iterator = events.iterator()
        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<GroupEntity>>()
            node.value shouldBe GROUP_A
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<GroupEntity>>()
            node.value shouldBe GROUP_B
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<EntryEntity>>()
            node.value shouldBe ENTRY_1
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<EntryEntity>>()
            node.value shouldBe ENTRY_2
        }
    }

    @Test
    fun `diff should work for entries with similar names`() {
        // arrange
        val lhs = dbTree(ROOT) {
            entry(ENTRY_1)
            entry(ENTRY_1_COPY)
            entry(ENTRY_2)
        }
        val rhs = dbTree(ROOT) {
            entry(ENTRY_1)
            entry(ENTRY_2)
            entry(ENTRY_2_COPY)
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 2

        val iterator = events.iterator()
        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<EntryEntity>>()
            node.value shouldBe ENTRY_1_COPY
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<EntryEntity>>()
            node.value shouldBe ENTRY_2_COPY
        }
    }

    @Test
    fun `diff should work for groups with similar names`() {
        // arrange
        val lhs = dbTree(ROOT) {
            group(GROUP_A)
            group(GROUP_A_COPY)
            group(GROUP_B)
        }
        val rhs = dbTree(ROOT) {
            group(GROUP_A)
            group(GROUP_B)
            group(GROUP_B_COPY)
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 2

        val iterator = events.iterator()
        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<GroupEntity>>()
            node.value shouldBe GROUP_A_COPY
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<GroupEntity>>()
            node.value shouldBe GROUP_B_COPY
        }
    }

    @Test
    fun `diff should work if group and entry has similar names`() {
        // arrange
        val lhs = dbTree(ROOT) {
            group(GROUP_ENTRY_1)
            entry(ENTRY_1)
            group(GROUP_ENTRY_2)
        }
        val rhs = dbTree(ROOT) {
            group(GROUP_ENTRY_1)
            group(GROUP_ENTRY_2)
            entry(ENTRY_2)
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 2

        // assert
        val iterator = events.iterator()
        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<EntryEntity>>()
            node.value shouldBe ENTRY_1
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<EntryEntity>>()
            node.value shouldBe ENTRY_2
        }
    }

    @Test
    fun `diff should work with complex structure`() {
        // arrange
        val lhs = dbTree(ROOT) {
            group(GROUP_A) {
                entry(ENTRY_1)
                entry(ENTRY_2)
                group(GROUP_B) {
                    entry(ENTRY_3)
                    entry(ENTRY_4)
                }
                group(GROUP_C) {
                    entry(ENTRY_5)
                    entry(ENTRY_6)
                }
                group(GROUP_D)
            }
        }
        val rhs = dbTree(ROOT) {
            group(GROUP_A) {
                entry(ENTRY_1_MODIFIED)
                entry(ENTRY_1_COPY)
                entry(ENTRY_2)
                group(GROUP_B) {
                    entry(ENTRY_3)
                    entry(ENTRY_4)
                    group(GROUP_C) {
                        entry(ENTRY_5)
                    }
                    group(GROUP_E)
                }
            }
        }

        // act
        val events = PathDatabaseDiffer().getDiff(lhs, rhs).events.sortForAssertion()

        // assert
        events.size shouldBe 9

        // groups
        val iterator = events.iterator()
        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<GroupEntity>>()
            node.value shouldBe GROUP_C
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<GroupEntity>>()
            node.value shouldBe GROUP_D
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<GroupEntity>>()
            node.value shouldBe GROUP_C
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<GroupEntity>>()
            node.value shouldBe GROUP_E
        }

        // entries
        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<EntryEntity>>()
            node.value shouldBe ENTRY_5
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<EntryEntity>>()
            node.value shouldBe ENTRY_6
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<EntryEntity>>()
            node.value shouldBe ENTRY_1_COPY
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<EntryEntity>>()
            node.value shouldBe ENTRY_5
        }

        // fields
        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Update<FieldEntity>>()

            oldNode.value.name shouldBe PROPERTY_USERNAME
            oldNode.value.value shouldBe ENTRY_1.getUsername()

            newNode.value.name shouldBe PROPERTY_USERNAME
            newNode.value.value shouldBe ENTRY_1_MODIFIED.getUsername()
        }
    }



    companion object {
        private const val CUSTOM_PROPERTY_NAME = "custom-property-name"
        private const val CUSTOM_PROPERTY_VALUE = "custom-property-value"

        private val GROUP_UID_RANGE = (0..99)
        private val ENTRY_UID_RANGE = (100..200)

        private val ROOT = GroupEntity(createUuidFrom(1), "Root Group")
        private val GROUP_A = GroupEntity(createUuidFrom(2), "Group A")
        private val GROUP_B = GroupEntity(createUuidFrom(3), "Group B")
        private val GROUP_C = GroupEntity(createUuidFrom(4), "Group C")
        private val GROUP_D = GroupEntity(createUuidFrom(5), "Group D")
        private val GROUP_E = GroupEntity(createUuidFrom(6), "Group E")

        private val GROUP_ENTRY_1 = GroupEntity(createUuidFrom(81), "Entry 1")
        private val GROUP_ENTRY_2 = GroupEntity(createUuidFrom(82), "Entry 2")

        private val GROUP_A_COPY = GROUP_A.copy(uuid = createUuidFrom(91))
        private val GROUP_B_COPY = GROUP_B.copy(uuid = createUuidFrom(92))

        private val ENTRY_1 = newEntry(101)
        private val ENTRY_2 = newEntry(102)
        private val ENTRY_3 = newEntry(103)
        private val ENTRY_4 = newEntry(104)
        private val ENTRY_5 = newEntry(105)
        private val ENTRY_6 = newEntry(106)

        private val ENTRY_1_COPY = ENTRY_1.copy(uuid = createUuidFrom(111))
        private val ENTRY_2_COPY = ENTRY_2.copy(uuid = createUuidFrom(112))

        private val ENTRY_1_MODIFIED = ENTRY_1.modify(
            username = ENTRY_1.getUsername() + " modified"
        )

        private fun EntryEntity.modify(
            uuid: UUID = this.uuid,
            title: String = properties[PROPERTY_TITLE] ?: EMPTY,
            username: String = properties[PROPERTY_USERNAME] ?: EMPTY,
            password: String = properties[PROPERTY_PASSWORD] ?: EMPTY,
            url: String = properties[PROPERTY_URL] ?: EMPTY,
            notes: String = properties[PROPERTY_NOTES] ?: EMPTY,
            custom: Map<String, String> = emptyMap()
        ): EntryEntity {
            val newProperties = mutableMapOf(
                PROPERTY_TITLE to title,
                PROPERTY_USERNAME to username,
                PROPERTY_PASSWORD to password,
                PROPERTY_URL to url,
                PROPERTY_NOTES to notes
            )

            newProperties.putAll(custom)

            return copy(
                uuid = uuid,
                properties = newProperties
            )
        }

        private fun EntryEntity.getUsername(): String {
            return properties[PROPERTY_USERNAME] ?: EMPTY
        }

        private fun newEntry(
            id: Int,
            title: String? = null,
        ): EntryEntity {
            val index = id - ENTRY_UID_RANGE.first
            return EntryEntity(
                uuid = createUuidFrom(id),
                properties = mapOf(
                    PROPERTY_TITLE to (title ?: "Entry $index"),
                    PROPERTY_USERNAME to "Username $index",
                    PROPERTY_PASSWORD to "Password $index",
                    PROPERTY_URL to "Url $index",
                    PROPERTY_NOTES to "Notes $index"
                )
            )
        }
    }
}