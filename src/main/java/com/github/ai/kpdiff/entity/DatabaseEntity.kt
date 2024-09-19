package com.github.ai.kpdiff.entity

import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import com.github.ai.kpdiff.utils.StringUtils.EMPTY
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
    val fields: Map<String, String>,
    override val name: String = fields[FIELD_TITLE] ?: EMPTY,
    val binaries: List<Binary> = emptyList()
) : DatabaseEntity

data class Field<T>(
    override val uuid: UUID,
    override val name: String,
    val value: T
) : DatabaseEntity