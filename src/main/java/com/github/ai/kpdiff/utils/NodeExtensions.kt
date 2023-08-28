package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.entity.Node
import java.util.LinkedList
import java.util.UUID
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

fun Node<*>.buildDepthMap(): Map<UUID, Int> {
    val depthMap = HashMap<UUID, Int>()

    val nodes = LinkedList<Node<*>>()
    nodes.add(this)

    var level = 0
    val result = mutableListOf<Node<*>>()
    while (nodes.isNotEmpty()) {
        repeat(nodes.size) {
            val node = nodes.removeFirst()

            depthMap[node.uuid] = level
            result.add(node)

            for (child in node.nodes) {
                nodes.add(child)
            }
        }

        level++
    }

    return depthMap
}