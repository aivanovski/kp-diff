package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.utils.getEntity
import com.github.ai.kpdiff.utils.getTitle
import com.github.ai.kpdiff.utils.getTypeCharacter

class EntryEntityFormatter : EntityFormatter<EntryEntity> {

    override fun format(
        event: DiffEvent<EntryEntity>,
        indentation: String
    ): String {
        val type = event.getTypeCharacter()
        val title = event.getEntity().getTitle()
        return "$type$indentation $ENTRY '$title'"
    }

    companion object {
        internal const val ENTRY = "Entry"
    }
}