package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.GroupEntity
import kotlin.reflect.KClass

class EntityFormatterProvider {

    private val formatters: Map<KClass<*>, EntityFormatter<*>> = mapOf(
        GroupEntity::class to GroupEntityFormatter(),
        EntryEntity::class to EntryEntityFormatter(),
        FieldEntity::class to FieldEntityFormatter()
    )

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getFormatter(type: KClass<T>): EntityFormatter<T> {
        require(formatters.containsKey(type))

        return formatters[type] as EntityFormatter<T>
    }
}