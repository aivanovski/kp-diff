package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.domain.diff.formatter.TerminalOutputFormatter.Color
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.GroupEntity
import java.util.UUID

fun DiffEvent<*>.getParentUuid(): UUID? {
    return when (this) {
        is DiffEvent.Insert -> parentUuid
        is DiffEvent.Delete -> parentUuid
        is DiffEvent.Update -> newParentUuid
    }
}

fun DiffEvent<*>.getTypeCharacter(): String {
    return when (this) {
        is DiffEvent.Insert -> "+"
        is DiffEvent.Delete -> "-"
        is DiffEvent.Update -> "~"
    }
}

fun DiffEvent<*>.getColor(): Color {
    return when (this) {
        is DiffEvent.Insert -> Color.GREEN
        is DiffEvent.Delete -> Color.RED
        is DiffEvent.Update -> Color.YELLOW
    }
}

fun <T : Any> DiffEvent<T>.getEntity(): T {
    return when (this) {
        is DiffEvent.Insert -> entity
        is DiffEvent.Delete -> entity
        is DiffEvent.Update -> newEntity
    }
}

fun DiffEvent<DatabaseEntity>.sortOrder(): Int {
    return when (this) {
        is DiffEvent.Update -> {
            newEntity.sortOrder()
        }

        is DiffEvent.Delete -> {
            10 + entity.sortOrder()
        }

        is DiffEvent.Insert -> {
            20 + entity.sortOrder()
        }
    }
}

private fun DatabaseEntity.sortOrder(): Int {
    return when (this) {
        is GroupEntity -> 1
        is EntryEntity -> 2
        is FieldEntity -> 3
    }
}