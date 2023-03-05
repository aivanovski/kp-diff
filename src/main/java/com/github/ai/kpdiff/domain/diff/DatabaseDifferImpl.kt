package com.github.ai.kpdiff.domain.diff

import com.github.ai.kpdiff.domain.diff.simpleDiffer.SimpleDiffer
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.KeepassDatabase

class DatabaseDifferImpl : DatabaseDiffer {

    private val differ = SimpleDiffer()

    override fun getDiff(
        lhs: KeepassDatabase,
        rhs: KeepassDatabase
    ): List<DiffEvent<DatabaseEntity>> {
        return differ.diff(lhs.root, rhs.root)
    }
}