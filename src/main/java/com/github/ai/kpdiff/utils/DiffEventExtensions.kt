package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.domain.diff.formatter.TerminalOutputFormatter.Color
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.GroupEntity
import java.util.UUID

private const val UPDATE_EVENT_BASE_ORDER = 0
private const val DELETE_EVENT_BASE_ORDER = 10
private const val INSERT_EVENT_BASE_ORDER = 20

private const val GROUP_SORT_ORDER = 1
private const val ENTRY_SORT_ORDER = 2
private const val FIELD_SORT_ORDER = 3

fun DiffEvent<*>.getParentUuid(): UUID? {
    return when (this) {
        is DiffEvent.Insert -> parentUuid
        is DiffEvent.Delete -> parentUuid

        // Only for fields, newParentUuid should always match oldParentUuid
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
            UPDATE_EVENT_BASE_ORDER + newEntity.sortOrder()
        }

        is DiffEvent.Delete -> {
            DELETE_EVENT_BASE_ORDER + entity.sortOrder()
        }

        is DiffEvent.Insert -> {
            INSERT_EVENT_BASE_ORDER + entity.sortOrder()
        }
    }
}

private fun DatabaseEntity.sortOrder(): Int {
    return when (this) {
        is GroupEntity -> GROUP_SORT_ORDER
        is EntryEntity -> ENTRY_SORT_ORDER
        is FieldEntity -> FIELD_SORT_ORDER
    }
}

fun <T> DiffEvent<DatabaseEntity>.chooseSourceByEventType(
    lhs: T,
    rhs: T
): T {
    return when (this) {
        is DiffEvent.Delete -> lhs
        is DiffEvent.Insert -> rhs
        is DiffEvent.Update -> rhs
    }
}