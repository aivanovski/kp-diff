package com.github.ai.kpdiff.testUtils

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import java.util.UUID

internal object AssertionDsl {

    fun List<DiffEvent<DatabaseEntity>>.shouldBe(
        content: DiffAssertionBuilder.() -> Unit
    ) {
        DiffAssertionBuilder(this)
            .apply {
                content.invoke(this)
            }
    }

    class DiffAssertionBuilder(
        private val actualEvents: List<DiffEvent<DatabaseEntity>>
    ) {

        private var index = 0

        fun size(size: Int) {
            actualEvents.size shouldBe size
        }

        fun update(
            oldParent: UUID,
            newParent: UUID,
            oldEntity: DatabaseEntity,
            newEntity: DatabaseEntity
        ) {
            val actualEvent = actualEvents[index]

            actualEvent should beInstanceOf<DiffEvent.Update<*>>()

            val updateEvent = actualEvent as DiffEvent.Update

            updateEvent.oldParentUuid shouldBe oldParent
            updateEvent.newParentUuid shouldBe newParent

            updateEvent.oldEntity shouldBe oldEntity
            updateEvent.newEntity shouldBe newEntity

            index++
        }

        fun delete(
            parent: UUID,
            entity: DatabaseEntity
        ) {
            val actualEvent = actualEvents[index]

            actualEvent should beInstanceOf<DiffEvent.Delete<*>>()

            val deleteEvent = actualEvent as DiffEvent.Delete
            deleteEvent.parentUuid shouldBe parent
            deleteEvent.entity shouldBe entity

            index++
        }

        fun insert(
            parent: UUID,
            entity: DatabaseEntity
        ) {
            val actualEvent = actualEvents[index]

            actualEvent should beInstanceOf<DiffEvent.Insert<*>>()

            val insertEvent = actualEvent as DiffEvent.Insert
            insertEvent.parentUuid shouldBe parent
            insertEvent.entity shouldBe entity

            index++
        }
    }
}