package com.github.ai.kpdiff.domain.diff

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffResult
import com.github.ai.kpdiff.entity.KeepassDatabase

interface DatabaseDiffer {
    fun getDiff(
        lhs: KeepassDatabase,
        rhs: KeepassDatabase
    ): DiffResult<KeepassDatabase, DatabaseEntity>
}