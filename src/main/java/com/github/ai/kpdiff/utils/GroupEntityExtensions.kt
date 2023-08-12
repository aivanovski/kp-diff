package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.EntryEntity.Companion.PROPERTY_TITLE
import com.github.ai.kpdiff.entity.EntryEntity.Companion.UUID
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.Node
import com.github.ai.kpdiff.entity.SimpleNode
import java.util.UUID

fun GroupEntity.getFields(): List<FieldEntity> {
    val uidField = FieldEntity(
        uuid = uuid,
        name = UUID,
        value = uuid.toString()
    ).also {
        it.entryUid = uuid
    }

    val titleField = FieldEntity(
        uuid = UUID(0, PROPERTY_TITLE.hashCode().toLong()),
        name = PROPERTY_TITLE,
        value = name
    ).also {
        it.entryUid = uuid
    }

    return listOf(uidField, titleField)
}

fun GroupEntity.getFieldNodes(): List<Node<DatabaseEntity>> {
    return this.getFields().map { field ->
        SimpleNode(
            uuid = field.uuid,
            value = field
        )
    }
}