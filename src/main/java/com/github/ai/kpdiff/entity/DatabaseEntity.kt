package com.github.ai.kpdiff.entity

import com.github.ai.kpdiff.utils.Properties.PROPERTY_TITLE
import com.github.ai.kpdiff.utils.StringUtils
import java.util.UUID

sealed interface DatabaseEntity {
    val uuid: UUID
    val name: String
}

data class GroupEntity(
    override val uuid: UUID,
    override val name: String
) : DatabaseEntity

data class EntryEntity(
    override val uuid: UUID,
    val properties: Map<String, String>,
    override val name: String = properties[PROPERTY_TITLE] ?: StringUtils.EMPTY
) : DatabaseEntity

data class FieldEntity(
    override val uuid: UUID,
    override val name: String,
    val value: String
) : DatabaseEntity