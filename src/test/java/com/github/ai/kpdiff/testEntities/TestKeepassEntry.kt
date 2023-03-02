package com.github.ai.kpdiff.testEntities

import java.util.UUID

data class TestKeepassEntry(
    val uuid: UUID,
    val title: String,
    val username: String,
    val password: String
)