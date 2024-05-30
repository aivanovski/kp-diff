package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.utils.buildAllEntryMap
import com.github.ai.kpdiff.utils.buildAllGroupMap
import com.github.ai.kpdiff.utils.chooseSourceByEventType
import com.github.ai.kpdiff.utils.getEntity
import com.github.ai.kpdiff.utils.getParentUuid

class ParentProvider(
    lhs: KeepassDatabase,
    rhs: KeepassDatabase
) {

    private val lhsGroupMap = lhs.buildAllGroupMap()
    private val lhsEntryMap = lhs.buildAllEntryMap()

    private val rhsGroupMap = rhs.buildAllGroupMap()
    private val rhsEntryMap = rhs.buildAllEntryMap()

    fun getParentName(event: DiffEvent<DatabaseEntity>): String {
        val parentUuid = event.getParentUuid()
        val entity = event.getEntity()

        val groupMap = event.chooseSourceByEventType(
            lhs = lhsGroupMap,
            rhs = rhsGroupMap
        )

        val entryMap = event.chooseSourceByEventType(
            lhs = lhsEntryMap,
            rhs = rhsEntryMap
        )

        return when (entity) {
            is GroupEntity -> groupMap[parentUuid]?.name ?: UNKNOWN_ENTITY
            is EntryEntity -> groupMap[parentUuid]?.name ?: UNKNOWN_ENTITY
            is FieldEntity -> {
                val parent = groupMap[parentUuid]
                    ?: entryMap[parentUuid]

                parent?.name ?: UNKNOWN_ENTITY
            }
        }
    }

    companion object {
        const val UNKNOWN_ENTITY = "Unknown entity"
    }
}