package com.github.ai.kpdiff.domain.diff.pathDiffer

import com.github.ai.kpdiff.domain.diff.DatabaseDiffer
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.DiffResult
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.PathNode
import com.github.ai.kpdiff.utils.convertToPathNodeTree
import com.github.ai.kpdiff.utils.getFields

class PathDatabaseDiffer : DatabaseDiffer {

    private val differ = PathDiffer()

    override fun getDiff(
        lhs: KeepassDatabase,
        rhs: KeepassDatabase
    ): DiffResult<KeepassDatabase, DatabaseEntity> {
        val lhsRoot = lhs.root.convertToPathNodeTree()
        val rhsRoot = rhs.root.convertToPathNodeTree()
        val diff = differ.diff(lhsRoot, rhsRoot)

        val events = mutableListOf<DiffEvent<DatabaseEntity>>()
        for (event in diff) {
            if (event is DiffEvent.Update &&
                event.newNode.value is EntryEntity &&
                event.oldNode.value is EntryEntity
            ) {
                val oldEntry = event.oldNode.value as EntryEntity
                val newEntry = event.newNode.value as EntryEntity

                val lhsFieldTree = oldEntry.getFields().map { field ->
                    PathNode<DatabaseEntity>(
                        uuid = field.uuid,
                        path = field.name,
                        value = field
                    )
                }
                val rhsFieldTree = newEntry.getFields().map { field ->
                    PathNode<DatabaseEntity>(
                        uuid = field.uuid,
                        path = field.name,
                        value = field
                    )
                }

                val entryDiff = differ.diff(lhsFieldTree, rhsFieldTree, HashSet())
                if (entryDiff.isNotEmpty()) {
                    events.addAll(entryDiff)
                } else {
                    events.add(event)
                }
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