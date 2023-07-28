package com.github.ai.kpdiff.entity

import java.util.UUID

data class EntryEntity(
    override val uuid: UUID,
    val properties: Map<String, String>
) : DatabaseEntity {

    companion object {
        const val PROPERTY_TITLE = "Title"
        const val PROPERTY_USERNAME = "UserName"
        const val PROPERTY_PASSWORD = "Password"
        const val PROPERTY_URL = "URL"
        const val PROPERTY_NOTES = "Notes"
    }
}