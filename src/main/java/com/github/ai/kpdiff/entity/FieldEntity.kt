package com.github.ai.kpdiff.entity

import java.util.UUID

data class FieldEntity(
    override val uuid: UUID,
    val entryUid: UUID,
    val name: String,
    val value: String
) : DatabaseEntity