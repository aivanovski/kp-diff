package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.EntryEntity.Companion.PROPERTY_TITLE
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.Node
import com.github.ai.kpdiff.entity.SimpleNode
import com.github.ai.kpdiff.utils.StringUtils.EMPTY
import java.util.UUID

fun EntryEntity.getTitle(): String {
    return properties[PROPERTY_TITLE] ?: EMPTY
}

fun EntryEntity.getUuidField(): FieldEntity {
    return FieldEntity(
        uuid = uuid,
        name = EntryEntity.UUID,
        value = uuid.toString()
    ).also {
        it.entryUid = uuid
    }
}

fun EntryEntity.getFields(): List<FieldEntity> {
    val result = mutableListOf<FieldEntity>()

    for ((name, value) in this.properties) {
        // TODO: resolve hash collision
        val fieldUid = UUID(0, name.hashCode().toLong())
        result.add(
            FieldEntity(fieldUid, name, value)
                .also {
                    it.entryUid = uuid
                }
        )
    }

    return result
}

fun EntryEntity.getFieldNodes(): List<Node<DatabaseEntity>> {
    return this.getFields().map { field ->
        SimpleNode(
            uuid = field.uuid,
            value = field
        )
    }
}