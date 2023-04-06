package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.Parent
import com.github.ai.kpdiff.utils.getTitle

class ParentFormatter {

    fun format(parent: Parent, indentation: String): String {
        return when (parent.entity) {
            is GroupEntity -> "~$indentation $GROUP '${parent.entity.name}'"
            is EntryEntity -> {
                val title = parent.entity.getTitle()
                "~$indentation $ENTRY '$title'"
            }
            else -> throw IllegalArgumentException()
        }
    }

    companion object {
        internal const val ENTRY = "Entry"
        internal const val GROUP = "Group"
    }
}