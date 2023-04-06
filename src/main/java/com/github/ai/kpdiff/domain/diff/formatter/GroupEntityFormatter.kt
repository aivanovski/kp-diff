package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.utils.getEntity
import com.github.ai.kpdiff.utils.getTypeCharacter

class GroupEntityFormatter : EntityFormatter<GroupEntity> {

    override fun format(
        event: DiffEvent<GroupEntity>,
        indentation: String
    ): String {
        val type = event.getTypeCharacter()
        val entity = event.getEntity()
        return "$type$indentation $GROUP '${entity.name}'"
    }

    companion object {
        internal const val GROUP = "Group"
    }
}