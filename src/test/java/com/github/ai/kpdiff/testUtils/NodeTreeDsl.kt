package com.github.ai.kpdiff.testUtils

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.SimpleNode
import com.github.ai.kpdiff.entity.Named
import com.github.ai.kpdiff.entity.PathNode
import java.util.UUID

object NodeTreeDsl {

    fun <T : Any> tree(
        value: T,
        content: SimpleNodeTreeBuilder<T>.() -> Unit
    ): SimpleNode<T> {
        val root = SimpleNodeTreeBuilder(createUuidFrom(value), value)
            .apply {
                content.invoke(this)
            }
            .build()

        return root
    }

    fun <T : Named> pathTree(
        value: T,
        content: PathNodeTreeBuilder<T>.() -> Unit
    ): PathNode<T> {
        val root = PathNodeTreeBuilder(createUuidFrom(value), value.name, value)
            .apply {
                content.invoke(this)
            }
            .build()

        return root
    }

    fun dbTree(
        rootGroup: GroupEntity,
        content: (DatabaseNodeTreeBuilder.() -> Unit)? = null
    ): KeepassDatabase {
        val root = DatabaseNodeTreeBuilder(rootGroup.uuid, rootGroup)
            .apply {
                content?.invoke(this)
            }
            .build()

        return KeepassDatabase(root)
    }

    class SimpleNodeTreeBuilder<T : Any>(
        private val rootUid: UUID,
        private val root: T
    ) {

        private val nodes = mutableListOf<SimpleNode<T>>()

        fun node(value: T, content: (SimpleNodeTreeBuilder<T>.() -> Unit)? = null) {
            node(createUuidFrom(value), value, content)
        }

        fun node(uid: UUID, value: T, content: (SimpleNodeTreeBuilder<T>.() -> Unit)? = null) {
            val node = SimpleNodeTreeBuilder(uid, value)
                .apply {
                    content?.invoke(this)
                }
                .build()

            nodes.add(node)
        }

        fun build(): SimpleNode<T> {
            return SimpleNode(rootUid, root, nodes)
        }
    }

    class PathNodeTreeBuilder<T : Named>(
        private val rootUid: UUID,
        private val path: String,
        private val root: T
    ) {
        private val nodes = mutableListOf<PathNode<T>>()

        fun node(value: T, content: (PathNodeTreeBuilder<T>.() -> Unit)? = null) {
            node(createUuidFrom(value), value, content)
        }

        fun node(
            uid: UUID,
            value: T,
            content: (PathNodeTreeBuilder<T>.() -> Unit)? = null
        ) {
            val node = PathNodeTreeBuilder(uid, path + "/" + value.name, value)
                .apply {
                    content?.invoke(this)
                }
                .build()

            nodes.add(node)
        }

        fun build(): PathNode<T> {
            return PathNode(rootUid, path, root, nodes)
        }
    }

    class DatabaseNodeTreeBuilder(
        private val rootUid: UUID,
        private val root: DatabaseEntity
    ) {

        private val nodes = mutableListOf<SimpleNode<DatabaseEntity>>()

        fun group(
            group: GroupEntity,
            content: (DatabaseNodeTreeBuilder.() -> Unit)? = null
        ) {
            add(group.uuid, group, content)
        }

        fun entry(entry: EntryEntity) {
            add(entry.uuid, entry)
        }

        fun build(): SimpleNode<DatabaseEntity> {
            return SimpleNode(rootUid, root, nodes)
        }

        private fun add(
            uid: UUID,
            value: DatabaseEntity,
            content: (DatabaseNodeTreeBuilder.() -> Unit)? = null
        ) {
            val node = DatabaseNodeTreeBuilder(uid, value)
                .apply {
                    content?.invoke(this)
                }
                .build()

            nodes.add(node)
        }
    }
}