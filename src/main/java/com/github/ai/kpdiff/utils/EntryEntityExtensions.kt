package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.EntryEntity.Companion.PROPERTY_TITLE
import com.github.ai.kpdiff.utils.StringUtils.EMPTY

fun EntryEntity.getTitle(): String {
    return properties[PROPERTY_TITLE] ?: EMPTY
}