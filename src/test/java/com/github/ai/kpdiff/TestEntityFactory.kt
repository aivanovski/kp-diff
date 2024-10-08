package com.github.ai.kpdiff

import com.github.ai.kpdiff.entity.Binary
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.Field
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.Hash
import com.github.ai.kpdiff.testUtils.createUuidFrom
import com.github.ai.kpdiff.utils.Fields.FIELD_NOTES
import com.github.ai.kpdiff.utils.Fields.FIELD_PASSWORD
import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import com.github.ai.kpdiff.utils.Fields.FIELD_URL
import com.github.ai.kpdiff.utils.Fields.FIELD_USERNAME
import com.github.ai.kpdiff.utils.StringUtils
import java.util.UUID

object TestEntityFactory {

    private const val ENTRY_UUID_SHIFT = 0x1L
    private const val GROUP_UUID_SHIFT = 0xFFL

    fun newGroup(id: Int): GroupEntity =
        GroupEntity(
            uuid = createUuidFrom(id),
            name = "Group $id"
        )

    fun newGroup(name: String): GroupEntity {
        return GroupEntity(
            uuid = UUID(GROUP_UUID_SHIFT, name.hashCode().toLong()),
            name = name
        )
    }

    fun newEntry(
        uuid: UUID? = null,
        title: String = StringUtils.EMPTY,
        username: String = StringUtils.EMPTY,
        password: String = StringUtils.EMPTY,
        url: String = StringUtils.EMPTY,
        notes: String = StringUtils.EMPTY,
        custom: Map<String, String> = emptyMap(),
        binaries: List<Binary> = emptyList()
    ): EntryEntity {
        return EntryEntity(
            uuid = uuid ?: UUID(ENTRY_UUID_SHIFT, title.hashCode().toLong()),
            binaries = binaries,
            fields = mapOf(
                FIELD_TITLE to title,
                FIELD_USERNAME to username,
                FIELD_PASSWORD to password,
                FIELD_URL to url,
                FIELD_NOTES to notes
            )
                .plus(custom)
        )
    }

    fun newEntry(
        id: Int,
        title: String = "Entry $id",
        username: String = "Username $id",
        password: String = "Password $id",
        url: String = "Url $id",
        notes: String = "Notes $id",
        custom: Map<String, String> = emptyMap(),
        binaries: List<Binary> = emptyList()
    ): EntryEntity {
        return EntryEntity(
            uuid = createUuidFrom(id),
            binaries = binaries,
            fields = mapOf(
                FIELD_TITLE to title,
                FIELD_USERNAME to username,
                FIELD_PASSWORD to password,
                FIELD_URL to url,
                FIELD_NOTES to notes
            )
                .plus(custom)
        )
    }

    fun newField(
        id: Int,
        name: String = "Field name $id",
        value: String = "Field value $id"
    ): Field<String> {
        return Field(
            uuid = createUuidFrom(name),
            name = name,
            value = value
        )
    }

    fun <T> newField(
        name: String,
        value: T
    ): Field<T> {
        return Field(
            uuid = UUID(0, name.hashCode().toLong()),
            name = name,
            value = value
        )
    }

    fun newBinary(
        name: String,
        content: String
    ): Binary {
        val bytes = content.toByteArray()
        return Binary(
            name = name,
            data = bytes,
            hash = Hash.fromContentBytes(bytes)
        )
    }

    fun newBinary(index: Int): Binary {
        val bytes = "Byte content $index".toByteArray()
        return Binary(
            name = "file-$index.txt",
            data = bytes,
            hash = Hash.fromContentBytes(bytes)
        )
    }
}