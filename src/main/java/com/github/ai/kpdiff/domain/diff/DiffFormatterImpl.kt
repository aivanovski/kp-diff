package com.github.ai.kpdiff.domain.diff

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.Node
import com.github.ai.kpdiff.utils.traverseByValueType
import com.github.ai.kpdiff.utils.traverseWithParents
import java.util.Comparator
import java.util.UUID

class DiffFormatterImpl : DiffFormatter {

    private val comparator = Comparator<DiffEvent<DatabaseEntity>> { lhs, rhs ->
        lhs.sortOrder().compareTo(rhs.sortOrder())
    }

    override fun format(
        diff: List<DiffEvent<DatabaseEntity>>,
        lhs: KeepassDatabase,
        rhs: KeepassDatabase
    ): List<String> {
        val lhsGroupMap = lhs.buildAllGroupMap()
        val lhsUuidToParentMap = lhs.buildUuidToParentMap()

        val rhsGroupMap = rhs.buildAllGroupMap()
        val rhsUuidToParentMap = rhs.buildUuidToParentMap()

        val eventsByParentUuid = LinkedHashMap<UUID?, MutableList<DiffEvent<DatabaseEntity>>>()
        for (event in diff) {
            val node = event.getNode()
            val parentUid = rhsUuidToParentMap[node.uuid] ?: lhsUuidToParentMap[node.uuid]

            val events = eventsByParentUuid.getOrDefault(parentUid, mutableListOf())
                .apply {
                    add(event)
                }

            eventsByParentUuid[parentUid] = events
        }

        val lines = mutableListOf<String>()
        for (parentUuid in eventsByParentUuid.keys) {
            val events = eventsByParentUuid[parentUuid] ?: continue

            events.sortWith(comparator)

            val parentNames = getParentNames(
                firstParentUuid = parentUuid,
                originType = events.getOriginType(),
                lhsGroupMap = lhsGroupMap,
                lhsUuidToParentMap = lhsUuidToParentMap,
                rhsGroupMap = rhsGroupMap,
                rhsUuidToParentMap = rhsUuidToParentMap
            )

            for ((idx, name) in parentNames.withIndex()) {
                val indent = INDENT.repeat(idx)

                lines.add("~ $indent$GROUP '$name'")
            }

            val entryLevel = parentNames.size
            val entryIndent = INDENT.repeat(entryLevel)
            for (event in events) {
                lines.add(event.format(indentation = entryIndent))
            }
        }

        return lines
    }

    private fun getParentNames(
        firstParentUuid: UUID?,
        originType: OriginType,
        lhsGroupMap: Map<UUID, GroupEntity>,
        lhsUuidToParentMap: Map<UUID, UUID>,
        rhsGroupMap: Map<UUID, GroupEntity>,
        rhsUuidToParentMap: Map<UUID, UUID>
    ): List<String> {
        val groupMap = when (originType) {
            OriginType.LEFT -> lhsGroupMap
            OriginType.RIGHT -> rhsGroupMap
        }

        val uuidToParentMap = when (originType) {
            OriginType.LEFT -> lhsUuidToParentMap
            OriginType.RIGHT -> rhsUuidToParentMap
        }

        val uuids = if (firstParentUuid != null) {
            findParents(firstParentUuid, uuidToParentMap)
                .toMutableList()
                .apply {
                    add(0, firstParentUuid)
                }
                .reversed()
        } else {
            listOf(null)
        }

        val names = uuids.map { uuid ->
            if (uuid != null) {
                groupMap[uuid]?.name ?: uuid.toString()
            } else {
                ROOT
            }
        }

        return names
    }

    private fun findParents(uuid: UUID, uuidToParentMap: Map<UUID, UUID>): List<UUID> {
        val result = mutableListOf<UUID>()

        var current: UUID? = uuid
        while (current != null && uuidToParentMap.containsKey(uuid)) {
            val parent = uuidToParentMap[current]

            if (parent != null) {
                result.add(parent)
            }

            current = parent
        }

        return result
    }

    private fun <T : Any> List<DiffEvent<T>>.getOriginType(): OriginType {
        val types = this.map { event -> event.getOriginType() }

        val rightCount = types.count { type -> type == OriginType.RIGHT }
        val leftCount = types.count { type -> type == OriginType.LEFT }

        return if (leftCount > rightCount) {
            OriginType.LEFT
        } else {
            OriginType.RIGHT
        }
    }

    private fun <T : Any> DiffEvent<T>.getOriginType(): OriginType {
        return when (this) {
            is DiffEvent.Insert -> OriginType.RIGHT
            is DiffEvent.Delete -> OriginType.LEFT
            is DiffEvent.Update -> OriginType.RIGHT
        }
    }

    private fun DiffEvent<DatabaseEntity>.format(indentation: String): String {
        val eventType = formatType()
        val entity = getEntity()

        val entityType = when (entity) {
            is GroupEntity -> GROUP
            is EntryEntity -> ENTRY
            else -> entity::class.java.simpleName
        }

        val title = when (entity) {
            is EntryEntity -> entity.properties[PROPERTY_TITLE]
            is GroupEntity -> entity.name
            else -> entity.toString()
        }

        return "$eventType $indentation$entityType '$title'"
    }

    private fun <T : Any> DiffEvent<T>.formatType(): String {
        return when (this) {
            is DiffEvent.Insert -> "+"
            is DiffEvent.Delete -> "-"
            is DiffEvent.Update -> "~"
        }
    }

    private fun <T : Any> DiffEvent<T>.getEntity(): T {
        return when (this) {
            is DiffEvent.Insert -> node.value
            is DiffEvent.Delete -> node.value
            is DiffEvent.Update -> newNode.value
        }
    }

    private fun <T : Any> DiffEvent<T>.getNode(): Node<T> {
        return when (this) {
            is DiffEvent.Insert -> node
            is DiffEvent.Delete -> node
            is DiffEvent.Update -> newNode
        }
    }

    private fun <T : Any> DiffEvent<T>.shouldLookForRight(): Boolean {
        return when (this) {
            is DiffEvent.Insert -> true
            is DiffEvent.Delete -> false
            is DiffEvent.Update -> true
        }
    }

    private fun KeepassDatabase.buildAllGroupMap(): Map<UUID, GroupEntity> {
        return this.root.traverseByValueType(GroupEntity::class)
            .map { node -> node.value }
            .associateBy { group -> group.uuid }
    }

    private fun KeepassDatabase.buildUuidToParentMap(): Map<UUID, UUID> {
        val result = HashMap<UUID, UUID>()

        val parentToNodePairs = root.traverseWithParents()
        for ((parentNode, node) in parentToNodePairs) {
            if (parentNode == null) continue

            result[node.uuid] = parentNode.uuid
        }

        return result
    }

    private fun DiffEvent<DatabaseEntity>.sortOrder(): Int {
        return when (this) {
            is DiffEvent.Update -> {
                if (newNode.value is GroupEntity) 1 else 5
            }
            is DiffEvent.Delete -> {
                if (node.value is GroupEntity) 10 else 15
            }
            is DiffEvent.Insert -> {
                if (node.value is GroupEntity) 20 else 25
            }
        }
    }

    enum class OriginType {
        LEFT,
        RIGHT
    }

    companion object {
        private const val INDENT = "    "
        private const val ROOT = "Root"
        private const val GROUP = "Group"
        private const val ENTRY = "Entry"
        private const val PROPERTY_TITLE = "Title"
    }
}