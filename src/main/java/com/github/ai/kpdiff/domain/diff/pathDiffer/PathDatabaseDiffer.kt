package com.github.ai.kpdiff.domain.diff.pathDiffer

import com.github.ai.kpdiff.domain.diff.DatabaseDiffer
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.DiffResult
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.PathNode
import com.github.ai.kpdiff.utils.asUpdate
import com.github.ai.kpdiff.utils.convertToPathNodeTree
import com.github.ai.kpdiff.utils.getFieldNodes
import com.github.ai.kpdiff.utils.getUuidField

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
            when {
                event.isGroupUpdate() -> {
                    val groupDiff = event.asUpdate().splitGroupUpdateIntoFieldDiff()
                    events.addAll(groupDiff)
                }

                event.isEntryUpdate() -> {
                    val entryDiff = event.asUpdate().splitEntryUpdateIntoFieldDiff()
                    events.addAll(entryDiff)
                }

                else -> {
                    events.add(event)
                }
            }
        }

        return DiffResult(
            lhs = lhs,
            rhs = rhs,
            events = events
        )
    }

    private fun DiffEvent.Update<DatabaseEntity>.splitGroupUpdateIntoFieldDiff():
        List<DiffEvent<DatabaseEntity>> {
        val oldGroup = this.oldNode.value as GroupEntity
        val newGroup = this.newNode.value as GroupEntity

        val lhsFieldTree = oldGroup.getFieldNodes().map { node ->
            node.convertToPathNodeTree()
        }
        val rhsFieldTree = newGroup.getFieldNodes().map { node ->
            node.convertToPathNodeTree()
        }

        return differ.diff(lhsFieldTree, rhsFieldTree, HashSet())
    }

    private fun DiffEvent.Update<DatabaseEntity>.splitEntryUpdateIntoFieldDiff():
        List<DiffEvent<DatabaseEntity>> {
        val oldEntry = this.oldNode.value as EntryEntity
        val newEntry = this.newNode.value as EntryEntity

        val lhsFieldTree = oldEntry.getFieldNodes().map { node ->
            node.convertToPathNodeTree()
        }

        val rhsFieldTree = newEntry.getFieldNodes().map { node ->
            node.convertToPathNodeTree()
        }

        val entryDiff = differ.diff(lhsFieldTree, rhsFieldTree, HashSet())
        return if (entryDiff.isNotEmpty()) {
            entryDiff
        } else if (oldEntry.uuid != newEntry.uuid) {
            val oldUidField = oldEntry.getUuidField()
            val newUidField = newEntry.getUuidField()

            listOf(
                DiffEvent.Update(
                    oldNode = PathNode(
                        uuid = oldUidField.uuid,
                        path = oldUidField.name,
                        value = oldUidField
                    ),
                    newNode = PathNode(
                        uuid = newUidField.uuid,
                        path = newUidField.name,
                        value = newUidField
                    )
                )
            )
        } else {
            emptyList()
        }
    }

    private fun <T : DatabaseEntity> DiffEvent<T>.isGroupUpdate(): Boolean {
        return (
            this is DiffEvent.Update &&
                this.newNode.value is GroupEntity &&
                this.oldNode.value is GroupEntity
            )
    }

    private fun <T : DatabaseEntity> DiffEvent<T>.isEntryUpdate(): Boolean {
        return (
            this is DiffEvent.Update &&
                this.newNode.value is EntryEntity &&
                this.oldNode.value is EntryEntity
            )
    }
}