package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.utils.getEntity
import com.github.ai.kpdiff.utils.getTypeCharacter

class FieldEntityFormatter : EntityFormatter<FieldEntity> {

    override fun format(event: DiffEvent<FieldEntity>, indentation: String): String {
        val type = event.getTypeCharacter()
        val entity = event.getEntity()

        val result = StringBuilder("$type$indentation $FIELD '${entity.name}'")

        if (event is DiffEvent.Update) {
            val oldEntity = event.oldNode.value
            val newEntity = event.newNode.value

            result.append(": '${oldEntity.value}' $CHANGED_TO '${newEntity.value}'")
        } else {
            result.append(": '${entity.value}'")
        }

        return result.toString()
    }

    companion object {
        internal const val FIELD = "Field"
        internal const val CHANGED_TO = "Changed to"
    }
}