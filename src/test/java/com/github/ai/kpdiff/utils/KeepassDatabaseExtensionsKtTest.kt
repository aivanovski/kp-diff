package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.TestEntityFactory.newEntry
import com.github.ai.kpdiff.TestEntityFactory.newGroup
import com.github.ai.kpdiff.testUtils.NodeTreeDsl.dbTree
import com.github.ai.kpdiff.testUtils.createUuidFrom
import com.github.ai.kpdiff.utils.KeepassDatabaseExtensionsKtTest.Ids.ENTRY1
import com.github.ai.kpdiff.utils.KeepassDatabaseExtensionsKtTest.Ids.ENTRY2
import com.github.ai.kpdiff.utils.KeepassDatabaseExtensionsKtTest.Ids.ENTRY3
import com.github.ai.kpdiff.utils.KeepassDatabaseExtensionsKtTest.Ids.ENTRY4
import com.github.ai.kpdiff.utils.KeepassDatabaseExtensionsKtTest.Ids.GROUP1
import com.github.ai.kpdiff.utils.KeepassDatabaseExtensionsKtTest.Ids.GROUP2
import com.github.ai.kpdiff.utils.KeepassDatabaseExtensionsKtTest.Ids.GROUP3
import com.github.ai.kpdiff.utils.KeepassDatabaseExtensionsKtTest.Ids.GROUP4
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KeepassDatabaseExtensionsKtTest {

    @Test
    fun `buildAllGroupMap should create map with all groups`() {
        // arrange
        val expected = listOf(
            newGroup(GROUP1),
            newGroup(GROUP2),
            newGroup(GROUP3),
            newGroup(GROUP4)
        )
            .associateBy { it.uuid }

        // act
        val groupMap = TREE.buildAllGroupMap()

        // assert
        groupMap shouldBe expected
    }

    @Test
    fun `buildAllEntryMap should create map with all entries`() {
        // arrange
        val expected = listOf(
            newEntry(ENTRY1),
            newEntry(ENTRY2),
            newEntry(ENTRY3),
            newEntry(ENTRY4)
        )
            .associateBy { it.uuid }

        // act
        val entryMap = TREE.buildAllEntryMap()

        // assert
        entryMap shouldBe expected
    }

    @Test
    fun `buildUuidToParentMap should map entries uuid to parent uuid`() {
        // act
        val uuidToParentMap = TREE.buildUuidToParentMap()

        // assert
        uuidToParentMap shouldBe mapOf(
            createUuidFrom(GROUP2) to createUuidFrom(GROUP1),
            createUuidFrom(ENTRY1) to createUuidFrom(GROUP1),

            createUuidFrom(GROUP3) to createUuidFrom(GROUP2),
            createUuidFrom(GROUP4) to createUuidFrom(GROUP2),
            createUuidFrom(ENTRY2) to createUuidFrom(GROUP2),

            createUuidFrom(ENTRY3) to createUuidFrom(GROUP3),
            createUuidFrom(ENTRY4) to createUuidFrom(GROUP3)
        )
    }

    private object Ids {
        const val GROUP1 = 1
        const val GROUP2 = 2
        const val GROUP3 = 3
        const val GROUP4 = 4

        const val ENTRY1 = 101
        const val ENTRY2 = 102
        const val ENTRY3 = 103
        const val ENTRY4 = 104
    }

    companion object {
        private val TREE = dbTree(newGroup(GROUP1)) {
            group(newGroup(GROUP2)) {
                group(newGroup(GROUP3)) {
                    entry(newEntry(ENTRY3))
                    entry(newEntry(ENTRY4))
                }
                group(newGroup(GROUP4))
                entry(newEntry(ENTRY2))
            }
            entry(newEntry(ENTRY1))
        }
    }
}