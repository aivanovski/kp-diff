package com.github.ai.kpdiff.entity

import java.util.UUID

class BasicNode<T : Any>(
    override val uuid: UUID,
    override val value: T,
    val nodes: MutableList<BasicNode<T>> = mutableListOf()
) : Node<T> {
    override fun toString(): String {
        return "Node(value=$value)"
    }
}