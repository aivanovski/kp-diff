package com.github.ai.kpdiff.entity

sealed class DiffEvent<T : Any> {

    data class Insert<T : Any>(
        val node: Node<T>
    ) : DiffEvent<T>()

    data class Delete<T : Any>(
        val node: Node<T>
    ) : DiffEvent<T>()

    data class Update<T : Any>(
        val oldNode: Node<T>,
        val newNode: Node<T>
    ) : DiffEvent<T>()
}