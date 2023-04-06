package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.domain.diff.formatter.TerminalOutputFormatter.Color
import com.github.ai.kpdiff.entity.DiffEvent

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