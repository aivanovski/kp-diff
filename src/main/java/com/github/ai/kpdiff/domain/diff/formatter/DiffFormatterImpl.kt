package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.domain.diff.DiffFormatter
import com.github.ai.kpdiff.domain.diff.formatter.TerminalOutputFormatter.Color
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.DiffResult
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.Parent
import com.github.ai.kpdiff.entity.SimpleNode
import com.github.ai.kpdiff.utils.getColor
import com.github.ai.kpdiff.utils.getEntity
import com.github.ai.kpdiff.utils.getFields
import com.github.ai.kpdiff.utils.getNode
import com.github.ai.kpdiff.utils.sortOrder
import com.github.ai.kpdiff.utils.traverseByValueType
import com.github.ai.kpdiff.utils.traverseWithParents
import java.util.UUID
import kotlin.Comparator

class DiffFormatterImpl(
    private val formatterProvider: EntityFormatterProvider,
    private val parentFormatter: ParentFormatter,
    private val terminalOutputFormatter: TerminalOutputFormatter
) : DiffFormatter {

    private val comparator = Comparator<DiffEvent<DatabaseEntity>> { lhs, rhs ->
        lhs.sortOrder().compareTo(rhs.sortOrder())
    }

    private val defaultFieldComparator = DefaultFieldComparator()
    private val otherFieldComparator = NameFieldComparator()

    override fun format(
        diff: DiffResult<KeepassDatabase, DatabaseEntity>,
        options: DiffFormatterOptions
    ): List<String> {
        val lhsGroupMap = diff.lhs.buildAllGroupMap()
        val lhsEntryMap = diff.lhs.buildAllEntryMap()
        val lhsUuidToParentMap = diff.lhs.buildUuidToParentMap()

        val rhsGroupMap = diff.rhs.buildAllGroupMap()
        val rhsEntryMap = diff.rhs.buildAllEntryMap()
        val rhsUuidToParentMap = diff.rhs.buildUuidToParentMap()

        val eventsByParentUuid = LinkedHashMap<UUID?, MutableList<DiffEvent<DatabaseEntity>>>()
        for (event in diff.events) {
            val node = event.getNode()
            val originType = event.getOriginType()

            when (val value = node.value) {
                is FieldEntity -> {
                    val events = eventsByParentUuid.getOrDefault(value.entryUid, mutableListOf())
                        .apply {
                            add(event)
                        }

                    eventsByParentUuid[value.entryUid] = events
                }
                else -> {
                    val parentUid = when (originType) {
                        OriginType.LEFT -> lhsUuidToParentMap[node.uuid]
                        OriginType.RIGHT -> rhsUuidToParentMap[node.uuid]
                    }

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

                if (shouldPrintAdditionalInformation(event, options)) {
                    val infoIndent = INDENT.repeat(entryLevel + 1)
                    lines.addAll(formatAdditionalInformation(event, infoIndent, options))
                }
            }
        }

        return lines
    }

//    private fun determineP

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

    private fun shouldPrintAdditionalInformation(
        event: DiffEvent<*>,
        options: DiffFormatterOptions
    ): Boolean {
        return options.isVerboseOutput &&
            (event is DiffEvent.Insert || event is DiffEvent.Delete) &&
            event.getEntity() is EntryEntity
    }

    private fun <T : DatabaseEntity> formatAdditionalInformation(
        event: DiffEvent<T>,
        indent: String,
        options: DiffFormatterOptions
    ): List<String> {
        val entity = event.getEntity()
        if (entity !is EntryEntity) {
            return emptyList()
        }

        if (event !is DiffEvent.Insert && event !is DiffEvent.Delete) {
            return emptyList()
        }

        val result = mutableListOf<String>()

        val allFields = entity.getFields()

        val defaultFields = allFields.filter { field -> field.isDefault() }
            .sortedWith(defaultFieldComparator)

        val otherFields = allFields.filter { field -> !field.isDefault() }
            .sortedWith(otherFieldComparator)

        for (field in (defaultFields + otherFields)) {
            val node = SimpleNode(field.uuid, field)

            val newEvent = if (event is DiffEvent.Insert) {
                DiffEvent.Insert(node)
            } else {
                DiffEvent.Delete(node)
            }

            result.add(formatEvent(newEvent, indent, options))
        }

        return result
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

    private fun FieldEntity.isDefault(): Boolean {
        return DEFAULT_PROPERTIES.contains(this.name)
    }

    class NameFieldComparator : Comparator<FieldEntity> {
        override fun compare(lhs: FieldEntity, rhs: FieldEntity): Int {
            return lhs.name.compareTo(rhs.name)
        }
    }

    class DefaultFieldComparator : Comparator<FieldEntity> {
        override fun compare(lhs: FieldEntity, rhs: FieldEntity): Int {
            val lhsOrder = DEFAULT_PROPERTIES_ORDER[lhs.name]
            val rhsOrder = DEFAULT_PROPERTIES_ORDER[rhs.name]
            return (lhsOrder ?: 0).compareTo(rhsOrder ?: 0)
        }
    }

    enum class OriginType {
        LEFT,
        RIGHT
    }

    companion object {
        private const val INDENT = "    "

        private val DEFAULT_PROPERTIES_ORDER = mapOf(
            EntryEntity.PROPERTY_TITLE to 1,
            EntryEntity.PROPERTY_USERNAME to 2,
            EntryEntity.PROPERTY_PASSWORD to 3,
            EntryEntity.PROPERTY_URL to 4,
            EntryEntity.PROPERTY_NOTES to 5
        )

        private val DEFAULT_PROPERTIES = setOf(
            EntryEntity.PROPERTY_TITLE,
            EntryEntity.PROPERTY_USERNAME,
            EntryEntity.PROPERTY_PASSWORD,
            EntryEntity.PROPERTY_URL,
            EntryEntity.PROPERTY_NOTES
        )
    }
}