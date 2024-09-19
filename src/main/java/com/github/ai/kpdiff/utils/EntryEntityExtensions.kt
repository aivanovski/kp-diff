package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.Field
import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import com.github.ai.kpdiff.utils.StringUtils.EMPTY
import java.util.UUID

fun EntryEntity.getTitle(): String {
    return fields[FIELD_TITLE] ?: EMPTY
}

fun EntryEntity.getFieldEntities(): List<Field<*>> {
    val result = mutableListOf<Field<*>>()

    for ((name, value) in this.fields) {
        val fieldUid = UUID(0, name.hashCode().toLong())
        result.add(Field(fieldUid, name, value))
    }

    for (binary in this.binaries) {
        val fieldUid = UUID(0, binary.name.hashCode().toLong())
        result.add(Field(fieldUid, binary.name, binary.data))
    }

    return result
}