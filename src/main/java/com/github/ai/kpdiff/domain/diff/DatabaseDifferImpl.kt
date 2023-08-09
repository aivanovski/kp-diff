package com.github.ai.kpdiff.domain.diff

import com.github.ai.kpdiff.domain.diff.simpleDiffer.SimpleDiffer
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.DiffResult
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.utils.convertToBasicNodeTree
import com.github.ai.kpdiff.utils.getFieldNodes

class DatabaseDifferImpl : DatabaseDiffer {

    private val differ = SimpleDiffer()

    override fun getDiff(
        lhs: KeepassDatabase,
        rhs: KeepassDatabase
    ): DiffResult<KeepassDatabase, DatabaseEntity> {
        val lhsRoot = lhs.root.convertToBasicNodeTree()
        val rhsRoot = rhs.root.convertToBasicNodeTree()
        val diff = differ.diff(lhsRoot, rhsRoot)

        val events = mutableListOf<DiffEvent<DatabaseEntity>>()
        for (event in diff) {
            if (event is DiffEvent.Update &&
                event.newNode.value is EntryEntity &&
                event.oldNode.value is EntryEntity
            ) {
                val oldEntry = event.oldNode.value as EntryEntity
                val newEntry = event.newNode.value as EntryEntity

                val lhsTree = oldEntry.getFieldNodes()
                val rhsTree = newEntry.getFieldNodes()

                val entryDiff = differ.diff(lhsTree, rhsTree, HashSet())

                events.addAll(entryDiff)
            } else {
                events.add(event)
            }
        }

        return DiffResult(
            lhs = lhs,
            rhs = rhs,
            events = events
        )
    }
}