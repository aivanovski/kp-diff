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
    return underlying.content.group.buildNodeTree()
}

fun GroupEntity.toBuilderEntity(): BuilderGroupEntity {
    return BuilderGroupEntity(
        uuid = uuid,
        fields = mapOf(FIELD_TITLE to name)
    )
}

fun EntryEntity.toBuilderEntity(
    created: Instant = Instant.MIN,
    modified: Instant = Instant.MIN
): BuilderEntryEntity {
    return BuilderEntryEntity(
        uuid = uuid,
        created = created,
        modified = modified,
        expires = null,
        fields = fields,
        history = emptyList()
    )
}

fun DatabaseKey.PasswordKey.convert(): KeepassKey.PasswordKey {
    return KeepassKey.PasswordKey(password = password)
}