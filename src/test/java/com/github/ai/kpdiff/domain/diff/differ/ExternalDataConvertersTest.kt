package com.github.ai.kpdiff.domain.diff.differ

import com.github.ai.kpdiff.TestDataFactory.newEntry
import com.github.ai.kpdiff.TestDataFactory.newField
import com.github.ai.kpdiff.TestDataFactory.newGroup
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.testUtils.NodeTreeDsl.dbTree
import com.github.ai.kpdiff.testUtils.createUuidFrom
import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import com.github.ai.kpdiff.utils.traverse
import com.github.aivanovski.keepasstreediff.entity.DiffEvent as ExternalDiffEvent
import com.github.aivanovski.keepasstreediff.entity.Entity as ExternalEntity
import com.github.aivanovski.keepasstreediff.entity.EntryEntity as ExternalEntryEntity
import com.github.aivanovski.keepasstreediff.entity.FieldEntity as ExternalFieldEntity
import com.github.aivanovski.keepasstreediff.entity.GroupEntity as ExternalGroupEntity
import com.github.aivanovski.keepasstreediff.entity.TreeNode as ExternalNode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import java.util.LinkedList
import org.junit.jupiter.api.Test

class ExternalDataConvertersTest {

    @Test
    fun `toExternalEntity should convert internal entry and group to external entry and group`() {
        newEntry(ENTRY1_ID).toExternalEntity() shouldBe newExternalEntry(ENTRY1_ID)
        newGroup(GROUP1_ID).toExternalEntity() shouldBe newExternalGroup(GROUP1_ID)
    }

    @Test
    fun `toExternalEntity should throw exception for field`() {
        shouldThrow<IllegalArgumentException> {
            newField(FIELD1_ID).toExternalEntity()
        }
    }

    @Test
    fun `toInternalEntity should convert external entities to internal entities`() {
        newExternalGroup(GROUP1_ID).toInternalEntity() shouldBe newGroup(GROUP1_ID)
        newExternalEntry(ENTRY1_ID).toInternalEntity() shouldBe newEntry(ENTRY1_ID)
        newExternalField(FIELD1_ID).toInternalEntity() shouldBe newField(FIELD1_ID)
    }

    @Test
    fun `toInternalDiffEvent should convert external event to internal event`() {
        listOf(
            ExternalDiffEvent.Insert(
                parentUuid = createUuidFrom(GROUP1_ID),
                entity = newExternalEntry(ENTRY1_ID) as ExternalEntity
            ) to DiffEvent.Insert(
                parentUuid = createUuidFrom(GROUP1_ID),
                entity = newEntry(ENTRY1_ID)
            ),

            ExternalDiffEvent.Delete(
                parentUuid = createUuidFrom(GROUP1_ID),
                entity = newExternalEntry(ENTRY1_ID) as ExternalEntity
            ) to DiffEvent.Delete(
                parentUuid = createUuidFrom(GROUP1_ID),
                entity = newEntry(ENTRY1_ID)
            ),

            ExternalDiffEvent.Update(
                oldParentUuid = createUuidFrom(GROUP1_ID),
                newParentUuid = createUuidFrom(GROUP2_ID),
                oldEntity = newExternalEntry(ENTRY1_ID) as ExternalEntity,
                newEntity = newExternalEntry(ENTRY2_ID) as ExternalEntity
            ) to DiffEvent.Update(
                oldParentUuid = createUuidFrom(GROUP1_ID),
                newParentUuid = createUuidFrom(GROUP2_ID),
                oldEntity = newEntry(ENTRY1_ID),
                newEntity = newEntry(ENTRY2_ID)
            )
        ).forEach { (input, expected) ->
            input.toInternalDiffEvent() shouldBe expected
        }
    }

    @Test
    fun `toExternalNode should convert node tree to external node`() {
        // arrange
        val tree = dbTree(newGroup(GROUP1_ID)) {
            entry(newEntry(ENTRY1_ID))
            group(newGroup(GROUP2_ID)) {
                group(newGroup(GROUP3_ID)) {
                    entry(newEntry(ENTRY3_ID))
                    entry(newEntry(ENTRY4_ID))
                }
                entry(newEntry(ENTRY2_ID))
            }
        }
        val expected = tree.root.traverse()
            .map { node -> node.value.toExternalEntity() }

        // act
        val result = tree.root.toExternalNode().traverse()
            .map { node -> node.entity }

        // assert
        result shouldBe expected
    }

    private fun ExternalNode.traverse(): List<ExternalNode> {
        val nodes = LinkedList<ExternalNode>()
        nodes.add(this)

        val result = mutableListOf<ExternalNode>()
        while (nodes.isNotEmpty()) {
            repeat(nodes.size) {
                val node = nodes.removeFirst()

                result.add(node)

                for (child in node.nodes) {
                    nodes.add(child)
                }
            }
        }

        return result
    }

    private fun newExternalGroup(id: Int): ExternalGroupEntity {
        val group = newGroup(id)

        return ExternalGroupEntity(
            uuid = group.uuid,
            fields = mapOf(
                FIELD_TITLE to ExternalFieldEntity(
                    name = FIELD_TITLE,
                    value = group.name
                )
            )
        )
    }

    private fun newExternalEntry(id: Int): ExternalEntryEntity {
        val entry = newEntry(id)

        return ExternalEntryEntity(
            uuid = entry.uuid,
            fields = entry.fields.map { (name, value) ->
                Pair(name, ExternalFieldEntity(name, value))
            }
                .toMap()
        )
    }

    private fun newExternalField(id: Int): ExternalFieldEntity {
        val field = newField(id)

        return ExternalFieldEntity(
            name = field.name,
            value = field.value
        )
    }

    companion object {
        private const val GROUP1_ID = 1
        private const val GROUP2_ID = 2
        private const val GROUP3_ID = 3
        private const val ENTRY1_ID = 101
        private const val ENTRY2_ID = 102
        private const val ENTRY3_ID = 103
        private const val ENTRY4_ID = 104
        private const val FIELD1_ID = 1001
    }
}