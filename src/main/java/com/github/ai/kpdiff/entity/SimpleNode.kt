package com.github.ai.kpdiff.entity

import java.util.UUID

class SimpleNode<T : Any>(
    override val uuid: UUID,
    override val value: T,
    override val nodes: MutableList<SimpleNode<T>> = mutableListOf()
) : Node<T> {
    override fun toString(): String {
        return "Node(value=$value)"
    }
}