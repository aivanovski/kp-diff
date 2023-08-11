package com.github.ai.kpdiff.domain.diff.uuidDiffer

import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.Node
import com.github.ai.kpdiff.utils.traverse
import java.util.UUID

class UuidDiffer {

    fun <T : Any> diff(
        lhsRoot: Node<T>,
        rhsRoot: Node<T>
    ): List<DiffEvent<T>> {
        return diff(
            listOf(lhsRoot),
            listOf(rhsRoot),
            HashSet()
        )
    }

    fun <T : Any> diff(
        lhsRoots: List<Node<T>>,
        rhsRoots: List<Node<T>>,
        visited: MutableSet<UUID>
    ): List<DiffEvent<T>> {
        val lhsNodes = lhsRoots.flatMap { node -> node.traverse() }
        val rhsNodes = rhsRoots.flatMap { node -> node.traverse() }

        val lhsNodesMap = lhsNodes.associateBy { node -> node.uuid }
        val rhsNodesMap = rhsNodes.associateBy { node -> node.uuid }

        val uuids = HashSet<UUID>()
            .apply {
                addAll(lhsNodesMap.keys)
                addAll(rhsNodesMap.keys)
            }

        val patchList = mutableListOf<DiffEvent<T>>()

        for (uuid in uuids) {
            val lhs = lhsNodesMap[uuid]
            val rhs = rhsNodesMap[uuid]

            val isLhsVisited = lhs?.uuid in visited
            val isRhsVisited = rhs?.uuid in visited

            when {
                lhs != null && rhs != null && !isLhsVisited && !isRhsVisited -> {
                    if (lhs.value != rhs.value) {
                        patchList.add(DiffEvent.Update(lhs, rhs))
                    }

                    patchList.addAll(
                        diff(
                            lhsRoots = lhs.nodes,
                            rhsRoots = rhs.nodes,
                            visited = visited
                        )
                    )
                }

                // item was removed
                lhs != null && rhs == null && !isLhsVisited -> {
                    patchList.add(DiffEvent.Delete(lhs))
                }

                // item was added
                lhs == null && rhs != null && !isRhsVisited -> {
                    patchList.add(DiffEvent.Insert(rhs))
                }
            }

            lhs?.let { visited.add(it.uuid) }
            rhs?.let { visited.add(it.uuid) }
        }

        return patchList
    }
}