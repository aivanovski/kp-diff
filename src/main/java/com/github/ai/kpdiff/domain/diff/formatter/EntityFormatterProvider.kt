package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.domain.usecases.FormatFileSizeUseCase
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.Field
import com.github.ai.kpdiff.entity.GroupEntity
import kotlin.reflect.KClass

class EntityFormatterProvider(
    formatFileSizeUseCase: FormatFileSizeUseCase
) {

    private val formatters: Map<KClass<*>, EntityFormatter<*>> = mapOf(
        GroupEntity::class to GroupEntityFormatter(),
        EntryEntity::class to EntryEntityFormatter(),
        Field::class to FieldFormatter(formatFileSizeUseCase)
    )

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getFormatter(type: KClass<T>): EntityFormatter<T> {
        require(formatters.containsKey(type))

        return formatters[type] as EntityFormatter<T>
    }
}