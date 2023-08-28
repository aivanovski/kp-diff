package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.KeepassDatabase
import java.util.UUID

fun KeepassDatabase.buildAllGroupMap(): Map<UUID, GroupEntity> {
    return this.root.traverseByValueType(GroupEntity::class)
        .map { node -> node.value }
        .associateBy { group -> group.uuid }
}

fun KeepassDatabase.buildAllEntryMap(): Map<UUID, EntryEntity> {
    return this.root.traverseByValueType(EntryEntity::class)
        .map { node -> node.value }
        .associateBy { entry -> entry.uuid }
}

fun KeepassDatabase.buildUuidToParentMap(): Map<UUID, UUID> {
    val result = HashMap<UUID, UUID>()

    val parentToNodePairs = root.traverseWithParents()
    for ((parentNode, node) in parentToNodePairs) {
        if (parentNode == null) continue

        result[node.uuid] = parentNode.uuid
    }

    return result
}