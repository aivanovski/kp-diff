package com.github.ai.kpdiff.entity

import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import com.github.ai.kpdiff.utils.StringUtils.EMPTY
import java.time.Instant
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
    val timestamps: Timestamps = Timestamps.EMPTY,
    val binaries: List<Binary> = emptyList()
) : DatabaseEntity

data class FieldEntity(
    override val uuid: UUID,
    override val name: String,
    val value: String
) : DatabaseEntity

data class Timestamps(
    val created: Instant?,
    val modified: Instant?,
    val expires: Instant?,
) {

    companion object {
        val EMPTY = Timestamps(null, null, null)
    }
}