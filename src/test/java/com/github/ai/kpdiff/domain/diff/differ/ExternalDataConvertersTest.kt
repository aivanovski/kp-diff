package com.github.ai.kpdiff.domain.diff.differ

import com.github.ai.kpdiff.TestEntityFactory.newEntry
import com.github.ai.kpdiff.TestEntityFactory.newField
import com.github.ai.kpdiff.TestEntityFactory.newGroup
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.testUtils.NodeTreeDsl.dbTree
import com.github.ai.kpdiff.testUtils.createUuidFrom
import com.github.ai.kpdiff.utils.Properties.PROPERTY_PASSWORD
import com.github.ai.kpdiff.utils.Properties.PROPERTY_TITLE
import com.github.ai.kpdiff.utils.Properties.PROPERTY_USERNAME
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

    private fun newExternalGroup(id: Int): ExternalGroupEntity =
        ExternalGroupEntity(
            uuid = createUuidFrom(id),
            fields = mapOf(
                PROPERTY_TITLE to ExternalFieldEntity(
                    name = PROPERTY_TITLE,
                    value = "Group $id"
                )
            )
        )

    private fun newExternalEntry(id: Int): ExternalEntryEntity =
        ExternalEntryEntity(
            uuid = createUuidFrom(id),
            fields = mapOf(
                PROPERTY_TITLE to ExternalFieldEntity(
                    name = PROPERTY_TITLE,
                    value = "Title $id"
                ),
                PROPERTY_USERNAME to ExternalFieldEntity(
                    name = PROPERTY_USERNAME,
                    value = "Username $id"
                ),
                PROPERTY_PASSWORD to ExternalFieldEntity(
                    name = PROPERTY_PASSWORD,
                    value = "Password $id"
                )
            )
        )

    private fun newExternalField(id: Int): ExternalFieldEntity =
        ExternalFieldEntity(
            name = "Field name $id",
            value = "Filed value $id"
        )

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