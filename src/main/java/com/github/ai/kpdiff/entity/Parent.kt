package com.github.ai.kpdiff.entity

import java.util.UUID

data class Parent(
    val entity: DatabaseEntity?,
    val uuid: UUID
)