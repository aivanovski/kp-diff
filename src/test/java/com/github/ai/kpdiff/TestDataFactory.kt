package com.github.ai.kpdiff

import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.testUtils.createUuidFrom
import com.github.ai.kpdiff.utils.Fields.FIELD_NOTES
import com.github.ai.kpdiff.utils.Fields.FIELD_PASSWORD
import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import com.github.ai.kpdiff.utils.Fields.FIELD_URL
import com.github.ai.kpdiff.utils.Fields.FIELD_USERNAME
import java.util.UUID

object TestDataFactory {

    fun newGroup(id: Int): GroupEntity =
        GroupEntity(
            uuid = createUuidFrom(id),
            name = "Group $id"
        )

    fun newEntry(
        id: Int,
        title: String = "Entry $id",
        username: String = "Username $id",
        password: String = "Password $id",
        url: String = "Url $id",
        notes: String = "Notes $id",
        custom: Map<String, String> = emptyMap()
    ): EntryEntity {
        return EntryEntity(
            uuid = createUuidFrom(id),
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
    ): FieldEntity {
        return FieldEntity(
            uuid = createUuidFrom(name),
            name = name,
            value = value
        )
    }

    fun newField(
        name: String,
        value: String
    ): FieldEntity {
        return FieldEntity(
            uuid = UUID(0, name.hashCode().toLong()),
            name = name,
            value = value
        )
    }
}