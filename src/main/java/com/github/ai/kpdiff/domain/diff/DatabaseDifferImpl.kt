package com.github.ai.kpdiff.domain.diff

import com.github.ai.kpdiff.domain.diff.simpleDiffer.SimpleDiffer
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.utils.getFieldNodes

class DatabaseDifferImpl : DatabaseDiffer {

    private val differ = SimpleDiffer()

    override fun getDiff(
        lhs: KeepassDatabase,
        rhs: KeepassDatabase
    ): List<DiffEvent<DatabaseEntity>> {
        val diff = differ.diff(lhs.root, rhs.root)

        val result = mutableListOf<DiffEvent<DatabaseEntity>>()
        for (event in diff) {
            if (event is DiffEvent.Update &&
                event.newNode.value is EntryEntity &&
                event.oldNode.value is EntryEntity
            ) {
                val lhsTree = event.oldNode.value.getFieldNodes()
                val rhsTree = event.newNode.value.getFieldNodes()

                val entryDiff = differ.diff(lhsTree, rhsTree, HashSet())

                result.addAll(entryDiff)
            } else {
                result.add(event)
            }
        }

        return result
    }
}