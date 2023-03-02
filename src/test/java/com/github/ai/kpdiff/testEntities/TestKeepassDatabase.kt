package com.github.ai.kpdiff.testEntities

data class TestKeepassDatabase(
    val key: TestKeepassKey,
    val filename: String,
    val root: TestKeepassGroup
)