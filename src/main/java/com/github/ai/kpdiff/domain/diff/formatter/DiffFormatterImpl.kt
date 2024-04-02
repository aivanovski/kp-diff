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
import com.github.ai.kpdiff.utils.Fields
import com.github.ai.kpdiff.utils.buildAllEntryMap
import com.github.ai.kpdiff.utils.buildAllGroupMap
import com.github.ai.kpdiff.utils.buildUuidToParentMap
import com.github.ai.kpdiff.utils.getColor
import com.github.ai.kpdiff.utils.getEntity
import com.github.ai.kpdiff.utils.getFieldEntities
import java.util.UUID

class DiffFormatterImpl(
    private val formatterProvider: EntityFormatterProvider,
    private val parentFormatter: ParentFormatter,
    private val terminalOutputFormatter: TerminalOutputFormatter
) : DiffFormatter {

    private val decorator = DiffDecorator()

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

        val eventsByParentUuid = decorator.decorate(diff)

        val lines = mutableListOf<String>()
        for ((parentUuid, events) in eventsByParentUuid) {
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

        val allFields = entity.getFieldEntities()

        val defaultFields = allFields.filter { field -> field.isDefault() }
        val otherFields = allFields.filter { field -> !field.isDefault() }

        val fieldEvents = (defaultFields + otherFields)
            .map { field ->
                when (event) {
                    is DiffEvent.Insert -> {
                        DiffEvent.Insert(
                            parentUuid = event.parentUuid,
                            entity = field as DatabaseEntity
                        )
                    }

                    is DiffEvent.Delete -> {
                        DiffEvent.Delete(
                            parentUuid = event.parentUuid,
                            entity = field as DatabaseEntity
                        )
                    }

                    else -> error("Illegal event type: $event")
                }
            }

        val sortedFieldEvents = DiffEventSorter().sort(fieldEvents)

        for (fieldEvent in sortedFieldEvents) {
            result.add(formatEvent(fieldEvent, indent, options))
        }

        return result
    }

    private fun FieldEntity.isDefault(): Boolean {
        return DEFAULT_PROPERTIES.contains(this.name)
    }

    enum class OriginType {
        LEFT,
        RIGHT
    }

    companion object {
        private const val INDENT = "    "

        private val DEFAULT_PROPERTIES = setOf(
            Fields.FIELD_TITLE,
            Fields.FIELD_USERNAME,
            Fields.FIELD_PASSWORD,
            Fields.FIELD_URL,
            Fields.FIELD_NOTES
        )
    }
}