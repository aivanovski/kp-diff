package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.entity.Node
import io.github.anvell.kotpass.cryptography.EncryptedValue
import io.github.anvell.kotpass.database.Credentials
import io.github.anvell.kotpass.models.Entry
import io.github.anvell.kotpass.models.Group
import java.util.LinkedList

fun KeepassKey.toCredentials(): Credentials {
    return when (this) {
        is KeepassKey.PasswordKey -> {
            Credentials.from(EncryptedValue.fromString(password))
        }
    }
}

fun Group.buildNodeTree(): Node<DatabaseEntity> {
    val root: Node<DatabaseEntity> = Node(uuid, this.toEntity())

    val groups = LinkedList<Pair<Node<DatabaseEntity>, Group>>()
    groups.add(Pair(root, this))

    while (groups.isNotEmpty()) {
        val (node, group) = groups.poll()

        for (childGroup in group.groups) {
            val childNode: Node<DatabaseEntity> = Node(childGroup.uuid, childGroup.toEntity())

            node.nodes.add(childNode)
            groups.push(Pair(childNode, childGroup))
        }

        for (entry in group.entries) {
            val entryNode: Node<DatabaseEntity> = Node(entry.uuid, entry.toEntity())
            node.nodes.add(entryNode)
        }
    }

    return root
}

private fun Group.toEntity(): GroupEntity {
    return GroupEntity(
        uuid = uuid,
        name = name
    )
}

private fun Entry.toEntity(): EntryEntity {
    val properties = mutableMapOf<String, String>()

    for ((key, value) in fields.entries) {
        properties[key] = value.content
    }

    return EntryEntity(
        uuid = uuid,
        properties = properties
    )
}