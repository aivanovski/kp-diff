package com.github.ai.kpdiff.testUtils

import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.models.DatabaseElement
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.entity.Node
import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import com.github.ai.kpdiff.utils.buildNodeTree
import com.github.aivanovski.keepasstreebuilder.extensions.toByteArray
import com.github.aivanovski.keepasstreebuilder.model.Database
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey
import com.github.aivanovski.keepasstreebuilder.model.EntryEntity as BuilderEntryEntity
import com.github.aivanovski.keepasstreebuilder.model.GroupEntity as BuilderGroupEntity
import app.keemobile.kotpass.database.modifiers.binaries
import com.github.ai.kpdiff.entity.Hash
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.Instant

fun Database<DatabaseElement, KeePassDatabase>.toInputStream(): InputStream {
    return ByteArrayInputStream(this.toByteArray())
}

fun DatabaseKey.BinaryKey.toInputStream(): InputStream {
    return ByteArrayInputStream(this.binaryData)
}

fun Database<DatabaseElement, KeePassDatabase>.buildNodeTree(): Node<DatabaseEntity> {
    val allBinaries = underlying.binaries.entries
        .associate { (key, value) ->
            Hash(key.base64()) to value.rawContent
        }

    return underlying.content.group.buildNodeTree(allBinaries = allBinaries)
}

fun GroupEntity.toBuilderEntity(): BuilderGroupEntity {
    return BuilderGroupEntity(
        uuid = uuid,
        fields = mapOf(FIELD_TITLE to name)
    )
}

fun EntryEntity.toBuilderEntity(): BuilderEntryEntity {
    return BuilderEntryEntity(
        uuid = uuid,
        created = timestamps.created ?: Instant.now(),
        modified = timestamps.modified ?: Instant.now(),
        expires = timestamps.expires,
        fields = fields,
        history = emptyList(),
        binaries = emptyList()
    )
}

fun DatabaseKey.PasswordKey.convert(): KeepassKey.PasswordKey {
    return KeepassKey.PasswordKey(password = password)
}