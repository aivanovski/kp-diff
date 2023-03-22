package com.github.ai.kpdiff.testUtils

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.entity.Node
import com.github.ai.kpdiff.testEntities.TestKeepassDatabase
import com.github.ai.kpdiff.testEntities.TestKeepassEntry
import com.github.ai.kpdiff.testEntities.TestKeepassGroup
import com.github.ai.kpdiff.testEntities.TestKeepassKey
import com.github.ai.kpdiff.utils.toCredentials
import io.github.anvell.kotpass.database.KeePassDatabase
import io.github.anvell.kotpass.database.decode
import java.io.InputStream
import java.util.LinkedList
import java.util.UUID

fun TestKeepassDatabase.open(): KeePassDatabase {
    val fsProvider = ResourcesFileSystemProvider()
    return KeePassDatabase.decode(
        contentStream(),
        key.convert().toCredentials(fsProvider).unwrap()
    )
}

@Suppress("UNCHECKED_CAST")
fun Node<DatabaseEntity>.isContentEquals(expected: TestKeepassDatabase): Boolean {
    val leftGroups = LinkedList<Node<DatabaseEntity>>()
    leftGroups.add(this)

    val rightGroups = LinkedList<TestKeepassGroup>()
        .apply {
            add(expected.root)
        }

    while (leftGroups.isNotEmpty() || rightGroups.isNotEmpty()) {
        val left = leftGroups.pollFirst()
        val right = rightGroups.pollFirst()

        val leftGroup = (left.value as? GroupEntity) ?: return false

        if ((left == null || right == null) ||
            (left.nodes.size != right.entries.size + right.groups.size)
        ) {
            return false
        }

        if (leftGroup.uuid != right.uuid || leftGroup.name != right.name) {
            return false
        }

        val entryNodes = left.nodes
            .filter { it.value is EntryEntity }
            .map { it as Node<EntryEntity> }

        val groupNodes = left.nodes
            .filter { it.value is GroupEntity }

        val entryCount = entryNodes.size
        for (idx in 0 until entryCount) {
            val leftEntryData = entryNodes[idx].value.toMap()
            val rightEntryData = right.entries[idx].toMap()

            if (leftEntryData != rightEntryData) {
                return false
            }
        }

        leftGroups.addAll(groupNodes)
        rightGroups.addAll(right.groups)
    }

    return true
}

private fun EntryEntity.toMap(): Map<String, Any> {
    return mapOf(
        "UUID" to uuid.toString(),
        "Title" to properties.getOrDefault("Title", ""),
        "UserName" to properties.getOrDefault("UserName", ""),
        "Password" to properties.getOrDefault("Password", ""),
        "URL" to "",
        "Notes" to ""
    )
}

private fun TestKeepassEntry.toMap(): Map<String, String> {
    return mapOf(
        "UUID" to uuid.toString(),
        "Title" to title,
        "UserName" to username,
        "Password" to password,
        "URL" to "",
        "Notes" to ""
    )
}

fun TestKeepassDatabase.contentStream(): InputStream {
    return resourceAsStream(filename)
}

fun TestKeepassKey.convert(): KeepassKey {
    return when (this) {
        is TestKeepassKey.PasswordKey -> KeepassKey.PasswordKey(password)
        is TestKeepassKey.FileKey -> KeepassKey.FileKey(path)
    }
}

fun TestKeepassKey.asFileKey(): TestKeepassKey.FileKey {
    return this as TestKeepassKey.FileKey
}

fun TestKeepassKey.FileKey.contentStream(): InputStream {
    return resourceAsStream(path)
}

fun createUuidFrom(value: Any): UUID {
    return UUID(0, value.hashCode().toLong())
}