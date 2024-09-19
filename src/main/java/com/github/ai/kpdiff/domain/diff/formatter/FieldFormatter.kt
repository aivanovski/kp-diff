package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.domain.usecases.FormatFileSizeUseCase
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.Field
import com.github.ai.kpdiff.utils.getEntity
import com.github.ai.kpdiff.utils.getTypeCharacter

class FieldFormatter(
    private val formatFileSizeUseCase: FormatFileSizeUseCase
) : EntityFormatter<Field<*>> {

    override fun format(
        event: DiffEvent<Field<*>>,
        indentation: String
    ): String {
        val type = event.getTypeCharacter()
        val field = event.getEntity()

        val fieldType = when (field.value) {
            is String -> FIELD
            is ByteArray -> ATTACHMENT
            else -> throw IllegalArgumentException("Unsupported field: $field")
        }

        val result = StringBuilder("$type$indentation $fieldType '${field.name}'")

        if (event is DiffEvent.Update) {
            val oldEntity = event.oldEntity
            val newEntity = event.newEntity

            result.append(": '${oldEntity.value}' $CHANGED_TO '${newEntity.value}'")
        } else {
            when (field.value) {
                is String -> {
                    result.append(": '${field.value}'")
                }

                is ByteArray -> {
                    val fileSize = formatFileSizeUseCase.formatHumanReadableFileSize(
                        field.value.size.toLong()
                    )
                    result.append(" $fileSize")
                }
            }
        }

        return result.toString()
    }

    companion object {
        internal const val FIELD = "Field"
        internal const val ATTACHMENT = "Attachment"
        internal const val CHANGED_TO = "Changed to"
    }
}