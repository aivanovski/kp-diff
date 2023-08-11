package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.domain.diff.formatter.TerminalOutputFormatter.Color
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.Node

fun <T : Any> DiffEvent<T>.getTypeCharacter(): String {
    return when (this) {
        is DiffEvent.Insert -> "+"
        is DiffEvent.Delete -> "-"
        is DiffEvent.Update -> "~"
    }
}

fun <T : Any> DiffEvent<T>.getColor(): Color {
    return when (this) {
        is DiffEvent.Insert -> Color.GREEN
        is DiffEvent.Delete -> Color.RED
        is DiffEvent.Update -> Color.YELLOW
    }
}

fun <T : Any> DiffEvent<T>.getEntity(): T {
    return when (this) {
        is DiffEvent.Insert -> node.value
        is DiffEvent.Delete -> node.value
        is DiffEvent.Update -> newNode.value
    }
}

fun <T : Any> DiffEvent<T>.getNode(): Node<T> {
    return when (this) {
        is DiffEvent.Insert -> node
        is DiffEvent.Delete -> node
        is DiffEvent.Update -> newNode
    }
}

fun DiffEvent<DatabaseEntity>.sortOrder(): Int {
    return when (this) {
        is DiffEvent.Update -> {
            if (newNode.value is GroupEntity) 1 else 5
        }

        is DiffEvent.Delete -> {
            if (node.value is GroupEntity) 10 else 15
        }

        is DiffEvent.Insert -> {
            if (node.value is GroupEntity) 20 else 25
        }
    }
}