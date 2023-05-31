package com.github.ai.kpdiff.entity

data class DiffResult<INPUT, RESULT : Any>(
    val lhs: INPUT,
    val rhs: INPUT,
    val events: List<DiffEvent<RESULT>>
)