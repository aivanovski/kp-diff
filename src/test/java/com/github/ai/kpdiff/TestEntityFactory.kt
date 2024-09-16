package com.github.ai.kpdiff

import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.Timestamps
import com.github.ai.kpdiff.testUtils.createUuidFrom
import com.github.ai.kpdiff.utils.Fields.FIELD_NOTES
import com.github.ai.kpdiff.utils.Fields.FIELD_PASSWORD
import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import com.github.ai.kpdiff.utils.Fields.FIELD_URL
import com.github.ai.kpdiff.utils.Fields.FIELD_USERNAME
import com.github.ai.kpdiff.utils.StringUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.UUID

object TestEntityFactory {

    private const val ENTRY_UUID_SHIFT = 0x1L
    private const val GROUP_UUID_SHIFT = 0xFFL
    private val ISO_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val DEFAULT_TIMESTAMPS = Timestamps(
        created = parseDateAndTime("2020-01-01 12:00:00"),
        modified = parseDateAndTime("2020-01-01 12:00:00"),
        expires = null
    )

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
        timestamps: Timestamps = DEFAULT_TIMESTAMPS,
        custom: Map<String, String> = emptyMap()
    ): EntryEntity {
        return EntryEntity(
            uuid = uuid ?: UUID(ENTRY_UUID_SHIFT, title.hashCode().toLong()),
            fields = mapOf(
                FIELD_TITLE to title,
                FIELD_USERNAME to username,
                FIELD_PASSWORD to password,
                FIELD_URL to url,
                FIELD_NOTES to notes
            )
                .plus(custom),
            timestamps = timestamps
        )
    }

    fun newEntry(
        id: Int,
        title: String = "Entry $id",
        username: String = "Username $id",
        password: String = "Password $id",
        url: String = "Url $id",
        notes: String = "Notes $id",
        timestamps: Timestamps = DEFAULT_TIMESTAMPS,
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
                .plus(custom),
            timestamps = timestamps
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

    private fun parseDateAndTime(date: String): Instant {
        return try {
            val time = ISO_DATE_FORMAT.parse(date).time
            Instant.ofEpochMilli(time)
        } catch (exception: ParseException) {
            throw exception
        }
    }
}