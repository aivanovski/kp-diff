package com.github.ai.kpdiff.entity

import java.util.UUID

interface Node<T : Any> {
    val uuid: UUID
    val value: T
    val nodes: List<Node<T>>
}