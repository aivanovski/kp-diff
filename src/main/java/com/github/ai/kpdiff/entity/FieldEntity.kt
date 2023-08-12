package com.github.ai.kpdiff.entity

import java.util.UUID

data class FieldEntity(
    override val uuid: UUID,
    override val name: String,
    val value: String
) : DatabaseEntity {
    var entryUid: UUID = UUID(0, 0)
}