package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.entity.SimpleNode
import com.github.ai.kpdiff.entity.Named
import com.github.ai.kpdiff.entity.Node
import com.github.ai.kpdiff.entity.PathNode
import java.util.LinkedList
import kotlin.reflect.KClass

fun <T1 : Any, T2 : Any> Node<T1>.traverseByValueType(type: KClass<T2>): List<Node<T2>> {
    val nodes = LinkedList<Node<T1>>()
    nodes.add(this)

    val result = mutableListOf<Node<T2>>()
    while (nodes.isNotEmpty()) {
        repeat(nodes.size) {
            val node = nodes.removeFirst()

            if (node.value::class.java === type.java) {
                @Suppress("UNCHECKED_CAST")
                result.add(node as Node<T2>)
            }

            nodes.addAll(node.nodes)
        }
    }

    return result
}

fun <T : Any> Node<T>.traverseWithParents(): List<Pair<Node<T>?, Node<T>>> {
    val nodes = LinkedList<Pair<Node<T>?, Node<T>>>()
    nodes.add(Pair(null, this))

    val result = mutableListOf<Pair<Node<T>?, Node<T>>>()
    while (nodes.isNotEmpty()) {
        repeat(nodes.size) {
            val (parent, node) = nodes.removeFirst()
            result.add(Pair(parent, node))

            for (childNode in node.nodes) {
                nodes.add(Pair(node, childNode))
            }
        }
    }

    return result
}

fun <T : Any> Node<T>.traverse(): List<Node<T>> {
    val nodes = LinkedList<Node<T>>()
    nodes.add(this)

    val result = mutableListOf<Node<T>>()
    while (nodes.isNotEmpty()) {
        repeat(nodes.size) {
            val node = nodes.removeFirst()

            result.add(node)

            for (child in node.nodes) {
                nodes.add(child)
            }
        }
    }

    return result
}

fun <T : Any> PathNode<T>.traversePathNode(): List<PathNode<T>> {
    val nodes = LinkedList<PathNode<T>>()
    nodes.add(this)

    val result = mutableListOf<PathNode<T>>()
    while (nodes.isNotEmpty()) {
        repeat(nodes.size) {
            val node = nodes.removeFirst()

            result.add(node)

            for (child in node.nodes) {
                nodes.add(child)
            }
        }
    }

    return result
}

fun <T : Any> Node<T>.convertToSimpleNodeTree(): SimpleNode<T> {
    val nodes = LinkedList<Pair<SimpleNode<T>?, Node<T>>>()
    nodes.add(Pair(null, this))

    var root: SimpleNode<T>? = null
    while (nodes.isNotEmpty()) {
        val (parent, node) = nodes.removeFirst()

        val simpleNode = SimpleNode(
            uuid = node.uuid,
            value = node.value
        )

        parent?.nodes?.add(simpleNode)
        if (root == null) {
            root = simpleNode
        }

        for (childNode in node.nodes) {
            nodes.add(Pair(simpleNode, childNode))
        }
    }

    return root ?: throw IllegalStateException()
}

fun <T : Named> Node<T>.convertToPathNodeTree(): PathNode<T> {
    val nodes = LinkedList<Pair<PathNode<T>?, Node<T>>>()
    nodes.add(Pair(null, this))

    var root: PathNode<T>? = null
    while (nodes.isNotEmpty()) {
        val (parent, node) = nodes.removeFirst()

        val path = if (parent != null) {
            parent.path + "/" + node.value.name
        } else {
            node.value.name
        }

        val pathNode = PathNode(
            uuid = node.uuid,
            path = path,
            value = node.value
        )

        parent?.nodes?.add(pathNode)
        if (root == null) {
            root = pathNode
        }

        for (childNode in node.nodes) {
            nodes.add(Pair(pathNode, childNode))
        }
    }

    return root ?: throw IllegalStateException()
}