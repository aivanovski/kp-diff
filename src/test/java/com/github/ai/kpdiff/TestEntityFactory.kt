package com.github.ai.kpdiff

import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.testUtils.createUuidFrom
import com.github.ai.kpdiff.utils.Properties
import java.util.UUID

object TestEntityFactory {

    fun newGroup(id: Int): GroupEntity =
        GroupEntity(
            uuid = createUuidFrom(id),
            name = "Group $id",
        )

    fun newEntry(id: Int): EntryEntity =
        EntryEntity(
            uuid = createUuidFrom(id),
            properties = mapOf(
                Properties.PROPERTY_TITLE to "Title $id",
                Properties.PROPERTY_USERNAME to "Username $id",
                Properties.PROPERTY_PASSWORD to "Password $id"
            )
        )

    fun newField(id: Int): FieldEntity {
        val name = "Field name $id"
        return FieldEntity(
            uuid = UUID(0, name.hashCode().toLong()),
            name = name,
            value = "Filed value $id"
        )
    }
}