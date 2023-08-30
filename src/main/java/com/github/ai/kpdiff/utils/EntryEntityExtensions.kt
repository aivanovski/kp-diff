package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import com.github.ai.kpdiff.utils.StringUtils.EMPTY
import java.util.UUID

fun EntryEntity.getTitle(): String {
    return fields[FIELD_TITLE] ?: EMPTY
}

fun EntryEntity.getFieldEntities(): List<FieldEntity> {
    val result = mutableListOf<FieldEntity>()

    for ((name, value) in this.fields) {
        val fieldUid = UUID(0, name.hashCode().toLong())
        result.add(FieldEntity(fieldUid, name, value))
    }

    return result
}