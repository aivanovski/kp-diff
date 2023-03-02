package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.testEntities.TestKeepassDatabase
import com.github.ai.kpdiff.testEntities.TestKeepassEntry
import com.github.ai.kpdiff.testEntities.TestKeepassGroup
import com.github.ai.kpdiff.testEntities.TestKeepassKey
import io.github.anvell.kotpass.models.Entry
import io.github.anvell.kotpass.models.Group
import java.io.InputStream
import java.util.LinkedList

fun KeepassDatabase.isContentEquals(expected: TestKeepassDatabase): Boolean {
    val leftGroups = LinkedList<Group>()
        .apply {
            add(root)
        }

    val rightGroups = LinkedList<TestKeepassGroup>()
        .apply {
            add(expected.root)
        }

    while (leftGroups.isNotEmpty() || rightGroups.isNotEmpty()) {
        val left = leftGroups.pollFirst()
        val right = rightGroups.pollFirst()

        if ((left == null || right == null) ||
            (left.entries.size != right.entries.size) ||
            (left.groups.size != right.groups.size)
        ) {
            return false
        }

        if (left.uuid != right.uuid || left.name != right.name) {
            return false
        }

        val entryCount = left.entries.size
        for (idx in 0 until entryCount) {
            val leftEntryData = left.entries[idx].toMap()
            val rightEntryData = right.entries[idx].toMap()

            if (leftEntryData != rightEntryData) {
                return false
            }
        }

        leftGroups.addAll(left.groups)
        rightGroups.addAll(right.groups)
    }

    return true
}

private fun TestKeepassEntry.toMap(): Map<String, Any> {
    return mapOf(
        "UUID" to uuid.toString(),
        "Title" to title,
        "UserName" to username,
        "Password" to password,
        "URL" to "",
        "Notes" to ""
    )
}

private fun Entry.toMap(): Map<String, Any> {
    return fields
        .map { entry -> Pair(entry.key, entry.value.content) }
        .toMap()
        .toMutableMap()
        .apply {
            this["UUID"] = uuid.toString()
        }
}

fun TestKeepassDatabase.contentStream(): InputStream {
    return resourceAsStream(filename)
}

fun TestKeepassKey.convert(): KeepassKey {
    return when (this) {
        is TestKeepassKey.PasswordKey -> KeepassKey.PasswordKey(password)
    }
}