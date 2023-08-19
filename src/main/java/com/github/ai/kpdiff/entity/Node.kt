package com.github.ai.kpdiff.entity

import java.util.UUID

class Node<T : Any>(
    val uuid: UUID,
    val value: T,
    val nodes: MutableList<Node<T>> = mutableListOf()
)