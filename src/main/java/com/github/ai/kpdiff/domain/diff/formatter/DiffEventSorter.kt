package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.DiffEventType
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.Field
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.utils.Fields.FIELD_NOTES
import com.github.ai.kpdiff.utils.Fields.FIELD_PASSWORD
import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import com.github.ai.kpdiff.utils.Fields.FIELD_URL
import com.github.ai.kpdiff.utils.Fields.FIELD_USERNAME
import com.github.ai.kpdiff.utils.getEntity
import kotlin.reflect.KClass

class DiffEventSorter {

    fun sort(events: List<DiffEvent<DatabaseEntity>>): List<DiffEvent<DatabaseEntity>> {
        return events.splitByEventType().values
            .map { eventsByType ->
                eventsByType.splitByEntityType()
                    .map { (type, eventsByEntityType) ->
                        if (type == Field::class) {
                            val (defaultFields, otherFields) = eventsByEntityType
                                .asFieldEvents()
                                .splitDefaultAndOtherFields()

                            defaultFields.sortDefaultFields() + otherFields.sortByName()
                        } else {
                            eventsByEntityType.sortByName()
                        }
                    }
            }
            .flatten()
            .flatten()
            .asEntityEvents()
    }

    private fun List<DiffEvent<DatabaseEntity>>.splitByEventType():
        Map<DiffEventType, List<DiffEvent<DatabaseEntity>>> {
        val updateEvents = this.mapNotNull { event ->
            event as? DiffEvent.Update<DatabaseEntity>
        }

        val deleteEvents = this.mapNotNull { event ->
            event as? DiffEvent.Delete<DatabaseEntity>
        }

        val insertEvents = this.mapNotNull { event ->
            event as? DiffEvent.Insert<DatabaseEntity>
        }

        return mapOf(
            DiffEventType.UPDATE to updateEvents,
            DiffEventType.DELETE to deleteEvents,
            DiffEventType.INSERT to insertEvents
        )
    }

    private fun List<DiffEvent<DatabaseEntity>>.splitByEntityType():
        Map<KClass<out DatabaseEntity>, List<DiffEvent<DatabaseEntity>>> {
        val groupEvents = this.mapNotNull { event ->
            if (event.getEntity() is GroupEntity) event else null
        }

        val entryEvents = this.mapNotNull { event ->
            if (event.getEntity() is EntryEntity) event else null
        }

        val fieldEvents = this.mapNotNull { event ->
            if (event.getEntity() is Field<*>) event else null
        }

        return mapOf(
            GroupEntity::class to groupEvents,
            EntryEntity::class to entryEvents,
            Field::class to fieldEvents
        )
    }

    private fun List<DiffEvent<Field<*>>>.splitDefaultAndOtherFields():
        Pair<List<DiffEvent<Field<*>>>, List<DiffEvent<Field<*>>>> {
        return this.partition { event ->
            val fieldName = event.getEntity().name
            fieldName in DEFAULT_FIELDS_ORDER.keys
        }
    }

    private fun <T : DatabaseEntity> List<DiffEvent<T>>.sortByName(): List<DiffEvent<T>> {
        return this.sortedBy { event -> event.getEntity().name }
    }

    private fun List<DiffEvent<Field<*>>>.sortDefaultFields(): List<DiffEvent<Field<*>>> {
        return this.sortedBy { event ->
            val fieldName = event.getEntity().name
            DEFAULT_FIELDS_ORDER[fieldName] ?: Int.MAX_VALUE
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun List<DiffEvent<DatabaseEntity>>.asFieldEvents(): List<DiffEvent<Field<*>>> {
        return this as List<DiffEvent<Field<*>>>
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : DatabaseEntity> List<DiffEvent<out T>>.asEntityEvents():
        List<DiffEvent<DatabaseEntity>> {
        return this as List<DiffEvent<DatabaseEntity>>
    }

    companion object {
        private val DEFAULT_FIELDS_ORDER = mapOf(
            FIELD_TITLE to 1,
            FIELD_USERNAME to 2,
            FIELD_PASSWORD to 3,
            FIELD_URL to 4,
            FIELD_NOTES to 5
        )
    }
}