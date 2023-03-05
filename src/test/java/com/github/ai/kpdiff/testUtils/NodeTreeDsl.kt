package com.github.ai.kpdiff.testUtils

import com.github.ai.kpdiff.entity.Node
import java.util.UUID

object NodeTreeDsl {

    fun <T : Any> tree(
        value: T,
        content: TreeBuilder<T>.() -> Unit
    ): Node<T> {
        val root = TreeBuilder(createUuidFrom(value), value)
            .apply {
                content.invoke(this)
            }
            .build()

        return root
    }

    class TreeBuilder<T : Any>(
        private val rootUid: UUID,
        private val root: T
    ) {

        private val nodes = mutableListOf<Node<T>>()

        fun node(value: T, content: (TreeBuilder<T>.() -> Unit)? = null) {
            node(createUuidFrom(value), value, content)
        }

        fun node(uid: T, value: T, content: (TreeBuilder<T>.() -> Unit)? = null) {
            node(createUuidFrom(uid), value, content)
        }

        private fun node(uid: UUID, value: T, content: (TreeBuilder<T>.() -> Unit)? = null) {
            val node = TreeBuilder(uid, value)
                .apply {
                    content?.invoke(this)
                }
                .build()

            nodes.add(node)
        }

        fun build(): Node<T> {
            return Node(rootUid, root, nodes)
        }
    }
}