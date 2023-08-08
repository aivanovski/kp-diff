package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.entity.BasicNode
import java.util.LinkedList
import kotlin.reflect.KClass

fun <T1 : Any, T2 : Any> BasicNode<T1>.traverseByValueType(type: KClass<T2>): List<BasicNode<T2>> {
    val nodes = LinkedList<BasicNode<T1>>()
    nodes.add(this)

    val result = mutableListOf<BasicNode<T2>>()
    while (nodes.isNotEmpty()) {
        repeat(nodes.size) {
            val node = nodes.removeFirst()

            if (node.value::class.java === type.java) {
                @Suppress("UNCHECKED_CAST")
                result.add(node as BasicNode<T2>)
            }

            nodes.addAll(node.nodes)
        }
    }

    return result
}

fun <T : Any> BasicNode<T>.traverseWithParents(): List<Pair<BasicNode<T>?, BasicNode<T>>> {
    val nodes = LinkedList<Pair<BasicNode<T>?, BasicNode<T>>>()
    nodes.add(Pair(null, this))

    val result = mutableListOf<Pair<BasicNode<T>?, BasicNode<T>>>()
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

fun <T : Any> BasicNode<T>.traverse(): List<BasicNode<T>> {
    val nodes = LinkedList<BasicNode<T>>()
    nodes.add(this)

    val result = mutableListOf<BasicNode<T>>()
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