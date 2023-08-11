package com.github.ai.kpdiff.entity

import java.util.UUID

data class GroupEntity(
    override val uuid: UUID,
    override val name: String
) : DatabaseEntity