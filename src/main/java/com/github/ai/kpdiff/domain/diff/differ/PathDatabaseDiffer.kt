package com.github.ai.kpdiff.domain.diff.differ

import com.github.ai.kpdiff.domain.diff.DatabaseDiffer
import com.github.aivanovski.keepasstreediff.PathDiffer
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffResult
import com.github.ai.kpdiff.entity.KeepassDatabase

class PathDatabaseDiffer(
    private val differ: PathDiffer
) : DatabaseDiffer {

    override fun getDiff(
        lhs: KeepassDatabase,
        rhs: KeepassDatabase
    ): DiffResult<KeepassDatabase, DatabaseEntity> {
        val diff = differ.diff(
            lhs = lhs.root.toExternalNode(),
            rhs = rhs.root.toExternalNode()
        )

        return DiffResult(
            lhs = lhs,
            rhs = rhs,
            events = diff.map { event -> event.toInternalDiffEvent() }
        )
    }
}