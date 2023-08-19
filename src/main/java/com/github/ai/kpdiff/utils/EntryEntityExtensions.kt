package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.utils.Properties.PROPERTY_TITLE
import com.github.ai.kpdiff.utils.StringUtils.EMPTY
import java.util.UUID

fun EntryEntity.getTitle(): String {
    return properties[PROPERTY_TITLE] ?: EMPTY
}

fun EntryEntity.getFields(): List<FieldEntity> {
    val result = mutableListOf<FieldEntity>()

    for ((name, value) in this.properties) {
        val fieldUid = UUID(0, name.hashCode().toLong())
        result.add(FieldEntity(fieldUid, name, value))
    }

    return result
}