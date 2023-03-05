package com.github.ai.kpdiff.entity

import java.util.UUID

data class EntryEntity(
    override val uuid: UUID,
    val properties: Map<String, String>
) : DatabaseEntity {

    companion object {
        const val PROPERTY_TITLE = "Title"
    }
}