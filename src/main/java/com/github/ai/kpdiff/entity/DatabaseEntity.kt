package com.github.ai.kpdiff.entity

import java.util.UUID

interface DatabaseEntity : Named {
    val uuid: UUID
    override val name: String
}