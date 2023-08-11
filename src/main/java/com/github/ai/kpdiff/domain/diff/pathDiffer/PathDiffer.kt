package com.github.ai.kpdiff.domain.diff.pathDiffer

import com.github.ai.kpdiff.domain.diff.uuidDiffer.UuidDiffer
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.PathNode
import com.github.ai.kpdiff.utils.getNode
import com.github.ai.kpdiff.utils.traversePathNode
import java.util.UUID

class PathDiffer {

    private val uuidDiffer = UuidDiffer()

    fun <T : Any> diff(
        lhsRoot: PathNode<T>,
        rhsRoot: PathNode<T>
    ): List<DiffEvent<T>> {
        return diff(
            listOf(lhsRoot),
            listOf(rhsRoot),
            HashSet()
        )
    }

    fun <T : Any> diff(
        lhsRoots: List<PathNode<T>>,
        rhsRoots: List<PathNode<T>>,
        visited: MutableSet<String>
    ): List<DiffEvent<T>> {
        val allLhsNodes = lhsRoots.flatMap { it.traversePathNode() }
        val allRhsNodes = rhsRoots.flatMap { it.traversePathNode() }

        val lhsNodesMap = groupNodesByPath(allLhsNodes)
        val rhsNodesMap = groupNodesByPath(allRhsNodes)

        val allPath = HashSet<String>()
            .apply {
                addAll(lhsNodesMap.keys)
                addAll(rhsNodesMap.keys)
            }

        val events = mutableListOf<DiffEvent<T>>()

        for (path in allPath) {
            val lhsNodes = lhsNodesMap[path] ?: emptyList()
            val rhsNodes = rhsNodesMap[path] ?: emptyList()

            // Process case when there are several
            if (lhsNodes.size > 1 || rhsNodes.size > 1) {
                if (path !in visited) {
                    val visitedUuids = HashSet<UUID>()
                    val diff = uuidDiffer.diff(lhsNodes, rhsNodes, visitedUuids)

                    // TODO: some child nodes may not be marked as visited
                    visited.add(path)
                    events.addAll(diff)
                }

                continue
            }

            val lhs = lhsNodes.firstOrNull()
            val rhs = rhsNodes.firstOrNull()

            val isLhsVisited = lhs?.path in visited
            val isRhsVisited = rhs?.path in visited

            when {
                lhs != null && rhs != null && !isLhsVisited && !isRhsVisited -> {
                    if (lhs.value != rhs.value || lhs.uuid != rhs.uuid) {
                        events.add(DiffEvent.Update(lhs, rhs))
                    }

                    events.addAll(diff(lhs.nodes, rhs.nodes, visited))
                }

                // item was removed
                lhs != null && rhs == null && !isLhsVisited -> {
                    events.add(DiffEvent.Delete(lhs))
                }

                // item was added
                lhs == null && rhs != null && !isRhsVisited -> {
                    events.add(DiffEvent.Insert(rhs))
                }
            }

            lhs?.let { node -> visited.add(node.path) }
            rhs?.let { node -> visited.add(node.path) }
        }

        return substituteEventsByUid(events)
    }

    private fun <T : Any> substituteEventsByUid(
        events: List<DiffEvent<T>>
    ): List<DiffEvent<T>> {
        val uidToEventsMap = groupEventsByUid(events)
        val result = mutableListOf<DiffEvent<T>>()

        for ((_, eventsByUid) in uidToEventsMap) {
            if (eventsByUid.size == 2) {
                val first = eventsByUid[0]
                val second = eventsByUid[1]

                val (deleteEvent, insertEvent) = when {
                    first is DiffEvent.Insert && second is DiffEvent.Delete -> {
                        second to first
                    }

                    first is DiffEvent.Delete && second is DiffEvent.Insert -> {
                        first to second
                    }

                    else -> {
                        result.addAll(eventsByUid)
                        continue
                    }
                }

                val deletedEntity = deleteEvent.node.value
                val insertedEntity = insertEvent.node.value
                if ((deletedEntity is EntryEntity || deletedEntity is GroupEntity) &&
                    deletedEntity == insertedEntity
                ) {
                    result.addAll(eventsByUid)
                } else {
                    result.add(
                        DiffEvent.Update(
                            oldNode = deleteEvent.node,
                            newNode = insertEvent.node
                        )
                    )
                }
            } else {
                result.addAll(eventsByUid)
            }
        }

        return result
    }

    private fun <T : Any> groupEventsByUid(
        events: List<DiffEvent<T>>
    ): Map<UUID, List<DiffEvent<T>>> {
        val uidToEventsMap = HashMap<UUID, MutableList<DiffEvent<T>>>()

        for (event in events) {
            val node = event.getNode()

            val eventsByUid = uidToEventsMap.getOrDefault(node.uuid, mutableListOf())
            eventsByUid.add(event)

            uidToEventsMap[node.uuid] = eventsByUid
        }

        return uidToEventsMap
    }

    private fun <T : Any> groupNodesByPath(
        nodes: List<PathNode<T>>
    ): Map<String, List<PathNode<T>>> {
        val pathToNodesMap = HashMap<String, MutableList<PathNode<T>>>()

        for (node in nodes) {
            pathToNodesMap[node.path] = pathToNodesMap.getOrDefault(node.path, mutableListOf())
                .apply {
                    add(node)
                }
        }

        return pathToNodesMap
    }
}