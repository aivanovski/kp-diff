package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.domain.diff.DiffFormatter
import com.github.ai.kpdiff.domain.diff.formatter.TerminalOutputFormatter.Color
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.Node
import com.github.ai.kpdiff.entity.Parent
import com.github.ai.kpdiff.utils.getColor
import com.github.ai.kpdiff.utils.getEntity
import com.github.ai.kpdiff.utils.traverseByValueType
import com.github.ai.kpdiff.utils.traverseWithParents
import java.util.Comparator
import java.util.UUID

class DiffFormatterImpl(
    private val formatterProvider: EntityFormatterProvider,
    private val parentFormatter: ParentFormatter,
    private val terminalOutputFormatter: TerminalOutputFormatter
) : DiffFormatter {

    private val comparator = Comparator<DiffEvent<DatabaseEntity>> { lhs, rhs ->
        lhs.sortOrder().compareTo(rhs.sortOrder())
    }

    override fun format(
        diff: List<DiffEvent<DatabaseEntity>>,
        lhs: KeepassDatabase,
        rhs: KeepassDatabase,
        options: DiffFormatterOptions
    ): List<String> {
        val lhsGroupMap = lhs.buildAllGroupMap()
        val lhsEntryMap = lhs.buildAllEntryMap()
        val lhsUuidToParentMap = lhs.buildUuidToParentMap()

        val rhsGroupMap = rhs.buildAllGroupMap()
        val rhsEntryMap = rhs.buildAllEntryMap()
        val rhsUuidToParentMap = rhs.buildUuidToParentMap()

        val eventsByParentUuid = LinkedHashMap<UUID?, MutableList<DiffEvent<DatabaseEntity>>>()
        for (event in diff) {
            val node = event.getNode()

            when (val value = node.value) {
                is FieldEntity -> {
                    val events = eventsByParentUuid.getOrDefault(value.entryUid, mutableListOf())
                        .apply {
                            add(event)
                        }

                    eventsByParentUuid[value.entryUid] = events
                }
                else -> {
                    val parentUid = rhsUuidToParentMap[node.uuid] ?: lhsUuidToParentMap[node.uuid]
                    val events = eventsByParentUuid.getOrDefault(parentUid, mutableListOf())
                        .apply {
                            add(event)
                        }

                    eventsByParentUuid[parentUid] = events
                }
            }
        }

        val lines = mutableListOf<String>()
        for (parentUuid in eventsByParentUuid.keys) {
            val events = eventsByParentUuid[parentUuid] ?: continue

            events.sortWith(comparator)

            val parents = getParents(
                firstParentUuid = parentUuid,
                originType = events.getOriginType(),
                lhsGroupMap = lhsGroupMap,
                lhsEntryMap = lhsEntryMap,
                lhsUuidToParentMap = lhsUuidToParentMap,
                rhsGroupMap = rhsGroupMap,
                rhsEntryMap = rhsEntryMap,
                rhsUuidToParentMap = rhsUuidToParentMap
            )

            for ((idx, parent) in parents.withIndex()) {
                val indent = INDENT.repeat(idx)
                lines.add(formatParent(parent, indent, options))
            }

            val entryLevel = parents.size
            val entryIndent = INDENT.repeat(entryLevel)
            for (event in events) {
                lines.add(formatEvent(event, entryIndent, options))
            }
        }

        return lines
    }

    private fun getParents(
        firstParentUuid: UUID?,
        originType: OriginType,
        lhsGroupMap: Map<UUID, GroupEntity>,
        lhsEntryMap: Map<UUID, EntryEntity>,
        lhsUuidToParentMap: Map<UUID, UUID>,
        rhsGroupMap: Map<UUID, GroupEntity>,
        rhsEntryMap: Map<UUID, EntryEntity>,
        rhsUuidToParentMap: Map<UUID, UUID>
    ): List<Parent> {
        val parents = mutableListOf<Parent>()

        val groupMap = when (originType) {
            OriginType.LEFT -> lhsGroupMap
            OriginType.RIGHT -> rhsGroupMap
        }

        val uuidToParentMap = when (originType) {
            OriginType.LEFT -> lhsUuidToParentMap
            OriginType.RIGHT -> rhsUuidToParentMap
        }

        val entryMap = when (originType) {
            OriginType.LEFT -> lhsEntryMap
            OriginType.RIGHT -> rhsEntryMap
        }

        var firstGroupUid: UUID? = firstParentUuid

        if (entryMap.containsKey(firstParentUuid)) {
            val entry = entryMap[firstParentUuid]
            if (firstParentUuid != null && entry != null) {
                parents.add(Parent(entry, firstParentUuid))
            }

            firstGroupUid = uuidToParentMap[firstParentUuid]
        }

        val parentUuids = if (firstGroupUid != null) {
            findParentUuids(firstGroupUid, uuidToParentMap)
        } else {
            emptyList()
        }

        for (parentUuid in parentUuids) {
            val group = groupMap[parentUuid]

            parents.add(Parent(group, parentUuid))
        }

        return parents.reversed()
    }

    private fun findParentUuids(uuid: UUID, uuidToParentMap: Map<UUID, UUID>): List<UUID> {
        val result = mutableListOf(uuid)

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

    private fun formatParent(
        parent: Parent,
        indent: String,
        options: DiffFormatterOptions
    ): String {
        return terminalOutputFormatter.format(
            line = parentFormatter.format(parent, indent),
            color = if (options.isColorEnabled) {
                Color.NONE
            } else {
                Color.NONE
            }
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : DatabaseEntity> formatEvent(
        event: DiffEvent<T>,
        indent: String,
        options: DiffFormatterOptions
    ): String {
        val entity = event.getEntity()
        val formatter = formatterProvider.getFormatter(entity::class)
            as EntityFormatter<DatabaseEntity>

        return terminalOutputFormatter.format(
            line = formatter.format(event as DiffEvent<DatabaseEntity>, indent),
            color = if (options.isColorEnabled) {
                event.getColor()
            } else {
                Color.NONE
            }
        )
    }

    private fun <T : Any> DiffEvent<T>.getNode(): Node<T> {
        return when (this) {
            is DiffEvent.Insert -> node
            is DiffEvent.Delete -> node
            is DiffEvent.Update -> newNode
        }
    }

    private fun KeepassDatabase.buildAllGroupMap(): Map<UUID, GroupEntity> {
        return this.root.traverseByValueType(GroupEntity::class)
            .map { node -> node.value }
            .associateBy { group -> group.uuid }
    }

    private fun KeepassDatabase.buildAllEntryMap(): Map<UUID, EntryEntity> {
        return this.root.traverseByValueType(EntryEntity::class)
            .map { node -> node.value }
            .associateBy { entry -> entry.uuid }
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
    }
}