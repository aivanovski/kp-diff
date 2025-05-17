package com.github.ai.kpdiff.utils

import app.keemobile.kotpass.cryptography.EncryptedValue
import app.keemobile.kotpass.database.Credentials
import app.keemobile.kotpass.models.Entry
import app.keemobile.kotpass.models.Group
import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.entity.Binary
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.Hash
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.entity.Node
import java.util.LinkedList

fun KeepassKey.toCredentials(fileSystemProvider: FileSystemProvider): Either<Credentials> {
    return when (this) {
        is KeepassKey.PasswordKey -> {
            Either.Right(
                Credentials.from(EncryptedValue.fromString(password))
            )
        }

        is KeepassKey.FileKey -> {
            val readKeyResult = fileSystemProvider.openForRead(path)
            if (readKeyResult.isLeft()) {
                return readKeyResult.mapToLeft()
            }

            val keyBytes = readKeyResult.unwrap().readAllBytes()
            Either.Right(Credentials.from(keyBytes))
        }

        is KeepassKey.CompositeKey -> {
            val readKeyResult = fileSystemProvider.openForRead(path)
            if (readKeyResult.isLeft()) {
                return readKeyResult.mapToLeft()
            }

            val keyBytes = readKeyResult.unwrap().readAllBytes()
            Either.Right(
                Credentials.from(
                    passphrase = EncryptedValue.fromString(password),
                    keyData = keyBytes
                )
            )
        }
    }
}

fun Group.buildNodeTree(allBinaries: Map<Hash, ByteArray>): Node<DatabaseEntity> {
    val root: Node<DatabaseEntity> = Node(uuid, this.toEntity())

    val groups = LinkedList<Pair<Node<DatabaseEntity>, Group>>()
    groups.add(Pair(root, this))

    while (groups.isNotEmpty()) {
        val (node, group) = groups.poll()

        for (childGroup in group.groups) {
            val childNode = Node<DatabaseEntity>(childGroup.uuid, childGroup.toEntity())

            node.nodes.add(childNode)
            groups.push(Pair(childNode, childGroup))
        }

        for (entry in group.entries) {
            val entryUid = entry.uuid
            val entryNode = Node<DatabaseEntity>(entryUid, entry.toEntity(allBinaries))

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

// TODO: This warning suppresses false-positive result in detekt
//  probably could be uncommented later
@Suppress("UnusedPrivateMember")
private fun Entry.toEntity(allBinaries: Map<Hash, ByteArray>): EntryEntity {
    val fields = fields.entries.associate { (key, value) ->
        key to value.content
    }

    val binaries = binaries.mapNotNull { binaryRef ->
        val hash = Hash(binaryRef.hash.base64())
        val data = allBinaries[hash]

        if (data != null) {
            Binary(
                name = binaryRef.name,
                hash = hash,
                data = data
            )
        } else {
            null
        }
    }

    return EntryEntity(
        uuid = uuid,
        fields = fields,
        binaries = binaries
    )
}