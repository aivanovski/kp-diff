package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.DiffResult
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.utils.buildDepthMap
import com.github.ai.kpdiff.utils.chooseSourceByEventType
import com.github.ai.kpdiff.utils.getParentUuid
import java.util.UUID

class DiffDecorator {

    fun decorate(
        diff: DiffResult<KeepassDatabase, DatabaseEntity>
    ): List<Pair<UUID?, List<DiffEvent<DatabaseEntity>>>> {
        val lhsDepthMap = diff.lhs.root.buildDepthMap()
        val rhsDepthMap = diff.rhs.root.buildDepthMap()
        val parentProvider = ParentProvider(
            lhs = diff.lhs,
            rhs = diff.rhs
        )

        val eventsByParentUuidMap = LinkedHashMap<UUID?, DataByParent>()
        for (event in diff.events) {
            val depth = getDepth(
                lhsDepthMap = lhsDepthMap,
                rhsDepthMap = rhsDepthMap,
                event = event
            )
            val parentName = parentProvider.getParentName(event)
            val parentUuid = event.getParentUuid()

            val dataByParent = eventsByParentUuidMap.getOrDefault(
                parentUuid,
                DataByParent(
                    parentUuid = parentUuid,
                    parentName = parentName,
                    treeDepth = depth,
                    events = mutableListOf()
                )
            )

            dataByParent.events.add(event)

            eventsByParentUuidMap[parentUuid] = dataByParent
        }

        val eventsByDepthMap = HashMap<Int, DataByDepth>()
        for ((_, dataByParent) in eventsByParentUuidMap) {
            val depth = dataByParent.treeDepth
            val parentUuid = dataByParent.parentUuid

            val dataByDepth = eventsByDepthMap.getOrDefault(
                depth,
                DataByDepth(
                    treeDepth = dataByParent.treeDepth,
                    events = HashMap()
                )
            )

            dataByDepth.events[parentUuid] = dataByParent

            eventsByDepthMap[depth] = dataByDepth
        }

        // sort data
        val sorter = DiffEventSorter()
        val allDepth = eventsByDepthMap.keys.sorted()
        val result = mutableListOf<Pair<UUID?, List<DiffEvent<DatabaseEntity>>>>()
        for (depth in allDepth) {
            val data = eventsByDepthMap[depth] ?: continue

            val eventsByParent = data.events.values.sortedBy { dataByParent ->
                dataByParent.parentName
            }

            for (dataByParent in eventsByParent) {
                val sortedEvents = sorter.sort(dataByParent.events)

                result.add(Pair(dataByParent.parentUuid, sortedEvents))
            }
        }

        return result
    }

    private fun getDepth(
        lhsDepthMap: Map<UUID, Int>,
        rhsDepthMap: Map<UUID, Int>,
        event: DiffEvent<DatabaseEntity>
    ): Int {
        val parentUuid = event.getParentUuid()
        val depthMap = event.chooseSourceByEventType(
            lhs = lhsDepthMap,
            rhs = rhsDepthMap
        )

        return depthMap[parentUuid]?.let { depth ->
            depth + 1
        } ?: 0
    }

    private data class DataByParent(
        val parentUuid: UUID?,
        val parentName: String,
        val treeDepth: Int,
        val events: MutableList<DiffEvent<DatabaseEntity>>
    )

    private data class DataByDepth(
        val treeDepth: Int,
        val events: MutableMap<UUID?, DataByParent>
    )
}