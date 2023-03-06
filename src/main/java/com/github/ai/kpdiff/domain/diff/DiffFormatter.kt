package com.github.ai.kpdiff.domain.diff

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.KeepassDatabase

interface DiffFormatter {
    fun format(
        diff: List<DiffEvent<DatabaseEntity>>,
        lhs: KeepassDatabase,
        rhs: KeepassDatabase,
        options: DiffFormatterOptions
    ): List<String>
}