package com.github.ai.kpdiff.domain.diff.differ

import com.github.ai.kpdiff.entity.Binary
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.Field
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.Hash
import com.github.ai.kpdiff.entity.Node
import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import com.github.aivanovski.keepasstreediff.entity.BinaryField
import com.github.aivanovski.keepasstreediff.entity.DiffEvent as ExternalDiffEvent
import com.github.aivanovski.keepasstreediff.entity.Entity as ExternalEntity
import com.github.aivanovski.keepasstreediff.entity.EntryEntity as ExternalEntryEntity
import com.github.aivanovski.keepasstreediff.entity.Field as ExternalField
import com.github.aivanovski.keepasstreediff.entity.GroupEntity as ExternalGroupEntity
import com.github.aivanovski.keepasstreediff.entity.MutableNode
import com.github.aivanovski.keepasstreediff.entity.StringField
import com.github.aivanovski.keepasstreediff.entity.TimestampField
import com.github.aivanovski.keepasstreediff.entity.TreeEntity as ExternalTreeEntity
import com.github.aivanovski.keepasstreediff.entity.TreeNode as ExternalTreeNode
import com.github.aivanovski.keepasstreediff.entity.UUIDField
import java.util.LinkedList
import java.util.UUID

fun ExternalDiffEvent<ExternalEntity>.toInternalDiffEvent(): DiffEvent<DatabaseEntity> {
    return when (this) {
        is ExternalDiffEvent.Update -> {
            DiffEvent.Update(
                oldParentUuid = oldParentUuid,
                newParentUuid = newParentUuid,
                oldEntity = oldEntity.toInternalEntity(),
                newEntity = newEntity.toInternalEntity()
            )
        }

        is ExternalDiffEvent.Delete -> {
            DiffEvent.Delete(
                parentUuid = parentUuid,
                entity = entity.toInternalEntity()
            )
        }

        is ExternalDiffEvent.Insert -> {
            DiffEvent.Insert(
                parentUuid = parentUuid,
                entity = entity.toInternalEntity()
            )
        }
    }
}

fun DatabaseEntity.toExternalEntity(): ExternalTreeEntity {
    return when (this) {
        is GroupEntity -> {
            ExternalGroupEntity(
                uuid = uuid,
                fields = mapOf(
                    FIELD_TITLE to StringField(
                        name = FIELD_TITLE,
                        value = this.name
                    )
                )
            )
        }

        is EntryEntity -> {
            val textFields = fields.entries.associate { (name, value) ->
                name to StringField(name, value)
            }

            val binaryFields = binaries.associate { binary ->
                Pair(
                    binary.name + "_" + binary.hash.value,
                    BinaryField(
                        hash = binary.hash.value,
                        name = binary.name,
                        value = binary.data
                    )
                )
            }

            ExternalEntryEntity(
                uuid = uuid,
                fields = textFields + binaryFields
            )
        }

        else -> {
            error("Should not be called for $this")
        }
    }
}

fun ExternalEntity.toInternalEntity(): DatabaseEntity {
    return when (this) {
        is ExternalGroupEntity -> {
            GroupEntity(
                uuid = uuid,
                // ExternalGroupEntity should always have PROPERTY_TITLE
                name = fields[FIELD_TITLE]?.getStringValue().orEmpty()
            )
        }

        is ExternalEntryEntity -> {
            val textFields = fields.entries
                .mapNotNull { (_, field) ->
                    if (field is StringField) {
                        field.name to field.value
                    } else {
                        null
                    }
                }
                .toMap()

            val binaries = fields.entries
                .mapNotNull { (_, field) ->
                    if (field is BinaryField) {
                        Binary(
                            name = field.name,
                            hash = Hash(field.hash),
                            data = field.value
                        )
                    } else {
                        null
                    }
                }

            EntryEntity(
                uuid = uuid,
                fields = textFields,
                binaries = binaries
            )
        }

        is ExternalField<*> -> toInternalField()

        else -> error("Illegal type: $this")
    }
}

private fun ExternalField<*>.toInternalField(): Field<*> {
    return when (this) {
        is StringField -> {
            Field(
                uuid = UUID(0, name.hashCode().toLong()),
                name = name,
                value = getStringValue()
            )
        }

        is BinaryField -> {
            Field(
                uuid = UUID(0, name.hashCode().toLong()),
                name = name,
                value = value
            )
        }

        is UUIDField -> {
            Field(
                uuid = UUID(0, name.hashCode().toLong()),
                name = name,
                value = value.toString()
            )
        }

        else -> error("Unsupported field type: $this")
    }
}

private fun ExternalField<*>.getStringValue(): String {
    return when (this) {
        is StringField -> value
        is TimestampField -> value.toString()
        is UUIDField -> value.toString()
        is BinaryField -> value.toString()
        else -> error("Unsupported field type: $this")
    }
}

fun <T : DatabaseEntity> Node<T>.toExternalNode(): ExternalTreeNode {
    val nodes = LinkedList<Pair<MutableNode?, Node<T>>>()
    nodes.add(Pair(null, this))

    var root: MutableNode? = null
    while (nodes.isNotEmpty()) {
        val (parent, node) = nodes.removeFirst()

        val newNode = MutableNode(
            entity = node.value.toExternalEntity()
        )

        parent?.nodes?.add(newNode)
        if (root == null) {
            root = newNode
        }

        for (childNode in node.nodes) {
            nodes.add(Pair(newNode, childNode))
        }
    }

    checkNotNull(root)

    return root
}