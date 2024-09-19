package com.github.ai.kpdiff.testUtils

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.DiffEventType
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.Field
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.utils.getEntity

/**
 * Sort diff events by entity type, then by event type and then by name.
 * Firstly goes Update events, then Delete events and then Insert events for [GroupEntity].
 * Then events goes is the same order for [EntryEntity] and then for [Field].
 *
 * Example:
 * 1. [DiffEvent.Update] with [GroupEntity]
 * 2. [DiffEvent.Delete] with [GroupEntity]
 * 3. [DiffEvent.Insert] with [GroupEntity]
 *
 * 4. [DiffEvent.Update] with [EntryEntity]
 * 5. [DiffEvent.Delete] with [EntryEntity]
 * 6. [DiffEvent.Insert] with [EntryEntity]
 *
 * 7. [DiffEvent.Update] with [Field]
 * 8. [DiffEvent.Delete] with [Field]
 * 9. [DiffEvent.Insert] with [Field]
 */
fun List<DiffEvent<DatabaseEntity>>.sortForAssertion(): List<DiffEvent<DatabaseEntity>> {
    val groupEvents = this.mapNotNull { event ->
        if (event.getEntity() is GroupEntity) event else null
    }

    val entryEvents = this.mapNotNull { event ->
        if (event.getEntity() is EntryEntity) event else null
    }

    val fieldEvents = this.mapNotNull { event ->
        if (event.getEntity() is Field<*>) event else null
    }

    val groupEventsSorted = groupEvents.splitByEventType()
        .values
        .map { events -> events.sortByName() }
        .flatten()

    val entryEventsSorted = entryEvents.splitByEventType()
        .values
        .map { events -> events.sortByName() }
        .flatten()

    val fieldEventsSorted = fieldEvents.splitByEventType()
        .values
        .map { events -> events.sortByName() }
        .flatten()

    return groupEventsSorted + entryEventsSorted + fieldEventsSorted
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

private fun List<DiffEvent<DatabaseEntity>>.sortByName(): List<DiffEvent<DatabaseEntity>> {
    return this.sortedBy { event -> event.getEntity().name }
}