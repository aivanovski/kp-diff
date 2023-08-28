package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.TestData.CUSTOM_PROPERTY_NAME
import com.github.ai.kpdiff.TestData.CUSTOM_PROPERTY_VALUE
import com.github.ai.kpdiff.TestDataFactory.newEntry
import com.github.ai.kpdiff.TestDataFactory.newField
import com.github.ai.kpdiff.TestDataFactory.newGroup
import com.github.ai.kpdiff.domain.diff.formatter.ParentProvider.Companion.UNKNOWN_ENTITY
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.ENTRY1
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.ENTRY2
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.ENTRY3
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.ENTRY4
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.ENTRY5
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.FIELD_CUSTOM
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.FIELD_TITLE
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.FIELD_USERNAME
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.GROUP1
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.GROUP2
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.GROUP3
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.GROUP4
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.NOT_EXIST
import com.github.ai.kpdiff.domain.diff.formatter.ParentProviderTest.Ids.ROOT
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.testUtils.NodeTreeDsl.dbTree
import com.github.ai.kpdiff.testUtils.createUuidFrom
import com.github.ai.kpdiff.utils.Fields
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ParentProviderTest {

    @Test
    fun `getParentName should return name for group events`() {
        listOf(
            DiffEvent.Insert(
                parentUuid = createUuidFrom(GROUP2),
                entity = newGroup(GROUP4)
            ) to newGroup(GROUP2).name,

            DiffEvent.Delete(
                parentUuid = createUuidFrom(GROUP1),
                entity = newGroup(GROUP3)
            ) to newGroup(GROUP1).name,

            DiffEvent.Insert(
                parentUuid = createUuidFrom(NOT_EXIST),
                entity = newGroup(NOT_EXIST)
            ) to UNKNOWN_ENTITY
        ).forEach { (data, expected) ->
            newProvider().getParentName(data.asDatabaseEvent()) shouldBe expected
        }
    }

    @Test
    fun `getParentName should return name for entry events`() {
        listOf(
            DiffEvent.Insert(
                parentUuid = createUuidFrom(GROUP2),
                entity = newEntry(ENTRY2)
            ) to newGroup(GROUP2).name,

            DiffEvent.Delete(
                parentUuid = createUuidFrom(GROUP1),
                entity = newEntry(ENTRY1)
            ) to newGroup(GROUP1).name,

            DiffEvent.Insert(
                parentUuid = createUuidFrom(NOT_EXIST),
                entity = newEntry(NOT_EXIST)
            ) to UNKNOWN_ENTITY
        ).forEach { (data, expected) ->
            newProvider().getParentName(data.asDatabaseEvent()) shouldBe expected
        }
    }

    @Test
    fun `getParentName should return name for entry fields`() {
        val customField = newField(
            id = FIELD_CUSTOM,
            name = CUSTOM_PROPERTY_NAME,
            value = CUSTOM_PROPERTY_VALUE
        )
        val oldUsernameField = newField(
            id = FIELD_USERNAME,
            name = Fields.FIELD_USERNAME,
            value = OLD_USERNAME
        )
        val newUsernameField = newField(
            id = FIELD_USERNAME,
            name = Fields.FIELD_USERNAME,
            value = NEW_USERNAME
        )
        val notExistingField = newField(
            id = NOT_EXIST,
            name = CUSTOM_PROPERTY_NAME,
            value = CUSTOM_PROPERTY_VALUE
        )

        listOf(
            DiffEvent.Insert(
                parentUuid = createUuidFrom(ENTRY3),
                entity = customField
            ) to newEntry(ENTRY3).name,

            DiffEvent.Delete(
                parentUuid = createUuidFrom(ENTRY4),
                entity = customField
            ) to newEntry(ENTRY4).name,

            DiffEvent.Update(
                oldParentUuid = createUuidFrom(ENTRY5),
                newParentUuid = createUuidFrom(ENTRY5),
                oldEntity = oldUsernameField,
                newEntity = newUsernameField
            ) to newEntry(ENTRY5).name,

            DiffEvent.Insert(
                parentUuid = createUuidFrom(NOT_EXIST),
                entity = notExistingField
            ) to UNKNOWN_ENTITY
        ).forEach { (data, expected) ->
            newProvider().getParentName(data.asDatabaseEvent()) shouldBe expected
        }
    }

    @Test
    fun `getParentName should return name for group fields`() {
        // arrange
        val oldTitleField = newField(
            id = FIELD_TITLE,
            name = Fields.FIELD_TITLE,
            value = "Title"
        )
        val newTitleField = newField(
            id = FIELD_TITLE,
            name = Fields.FIELD_TITLE,
            value = "New title"
        )
        val event = DiffEvent.Update(
            oldParentUuid = createUuidFrom(GROUP1),
            newParentUuid = createUuidFrom(GROUP1),
            oldEntity = oldTitleField,
            newEntity = newTitleField
        )

        // act & assert
        newProvider().getParentName(event.asDatabaseEvent()) shouldBe newGroup(GROUP1).name
    }

    @Suppress("UNCHECKED_CAST")
    private fun DiffEvent<*>.asDatabaseEvent(): DiffEvent<DatabaseEntity> =
        this as DiffEvent<DatabaseEntity>

    private fun newProvider(): ParentProvider =
        ParentProvider(
            lhs = LHS_DATABASE,
            rhs = RHS_DATABASE
        )

    private object Ids {
        const val ROOT = 0
        const val NOT_EXIST = 1000

        const val GROUP1 = 1
        const val GROUP2 = 2
        const val GROUP3 = 3
        const val GROUP4 = 4

        const val ENTRY1 = 11
        const val ENTRY2 = 12
        const val ENTRY3 = 13
        const val ENTRY4 = 14
        const val ENTRY5 = 15

        const val FIELD_TITLE = 21
        const val FIELD_USERNAME = 22
        const val FIELD_CUSTOM = 23
    }

    companion object {
        private const val OLD_USERNAME = "Old username"
        private const val NEW_USERNAME = "New username"

        private val LHS_DATABASE = dbTree(newGroup(ROOT)) {
            group(newGroup(GROUP1)) {
                entry(newEntry(ENTRY1))
                group(newGroup(GROUP3))
            }
            group(newGroup(GROUP2))
            entry(newEntry(ENTRY3, username = OLD_USERNAME))
            entry(newEntry(ENTRY4, custom = mapOf(CUSTOM_PROPERTY_NAME to CUSTOM_PROPERTY_VALUE)))
            entry(newEntry(ENTRY5))
        }

        private val RHS_DATABASE = dbTree(newGroup(ROOT)) {
            group(newGroup(GROUP1))
            group(newGroup(GROUP2)) {
                entry(newEntry(ENTRY2))
                group(newGroup(GROUP4))
            }
            entry(newEntry(ENTRY3, custom = mapOf(CUSTOM_PROPERTY_NAME to CUSTOM_PROPERTY_VALUE)))
            entry(newEntry(ENTRY4))
            entry(newEntry(ENTRY5, username = NEW_USERNAME))
        }
    }
}