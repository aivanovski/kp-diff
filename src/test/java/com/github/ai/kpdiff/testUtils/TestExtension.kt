package com.github.ai.kpdiff.testUtils

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.Node
import java.util.LinkedList
import java.util.UUID

@Suppress("UNCHECKED_CAST")
fun Node<DatabaseEntity>.isContentEquals(expected: Node<DatabaseEntity>): Boolean {
    val leftGroups = LinkedList<Node<DatabaseEntity>>()
    leftGroups.add(this)

    val rightGroups = LinkedList<Node<DatabaseEntity>>()
    rightGroups.add(expected)

    while (leftGroups.isNotEmpty() || rightGroups.isNotEmpty()) {
        val left = leftGroups.pollFirst()
        val right = rightGroups.pollFirst()

        val leftGroup = (left.value as? GroupEntity) ?: return false
        val rightGroup = (right.value as? GroupEntity) ?: return false

        if ((left == null || right == null) ||
            (left.nodes.size != right.nodes.size)
        ) {
            return false
        }

        if (leftGroup.uuid != rightGroup.uuid || leftGroup.name != rightGroup.name) {
            return false
        }

        val leftEntryNodes = left.nodes
            .filter { it.value is EntryEntity }
            .map { it as Node<EntryEntity> }

        val rightEntryNodes = right.nodes
            .filter { it.value is EntryEntity }
            .map { it as Node<EntryEntity> }

        val leftGroupNodes = left.nodes
            .filter { it.value is GroupEntity }

        val rightGroupNodes = right.nodes
            .filter { it.value is GroupEntity }

        val entryCount = leftEntryNodes.size
        for (idx in 0 until entryCount) {
            val leftEntryData = leftEntryNodes[idx].value.toMap()
            val rightEntryData = rightEntryNodes[idx].value.toMap()

            if (leftEntryData != rightEntryData) {
                return false
            }
        }

        leftGroups.addAll(leftGroupNodes)
        rightGroups.addAll(rightGroupNodes)
    }

    return true
}

private fun EntryEntity.toMap(): Map<String, Any> {
    return mapOf(
        "UUID" to uuid.toString(),
        "Title" to fields.getOrDefault("Title", ""),
        "UserName" to fields.getOrDefault("UserName", ""),
        "Password" to fields.getOrDefault("Password", ""),
        "URL" to "",
        "Notes" to ""
    )
}

fun createUuidFrom(value: Any): UUID {
    return UUID(0, value.hashCode().toLong())
}