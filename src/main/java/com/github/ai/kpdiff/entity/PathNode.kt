package com.github.ai.kpdiff.entity

import java.util.UUID

data class PathNode<T : Any>(
    override val uuid: UUID,
    val path: String,
    override val value: T,
    override val nodes: MutableList<PathNode<T>> = mutableListOf()
) : Node<T>