package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.entity.DiffEvent

interface EntityFormatter<T : Any> {
    fun format(
        event: DiffEvent<T>,
        indentation: String
    ): String
}