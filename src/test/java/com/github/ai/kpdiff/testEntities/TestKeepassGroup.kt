package com.github.ai.kpdiff.testEntities

import java.util.UUID

data class TestKeepassGroup(
    val uuid: UUID,
    val name: String,
    val entries: List<TestKeepassEntry>,
    val groups: List<TestKeepassGroup>
)