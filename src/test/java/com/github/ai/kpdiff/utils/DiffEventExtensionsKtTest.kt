package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.TestData.UUID_CHILD
import com.github.ai.kpdiff.TestData.UUID_PARENT
import com.github.ai.kpdiff.TestDataFactory.newEntry
import com.github.ai.kpdiff.TestDataFactory.newField
import com.github.ai.kpdiff.TestDataFactory.newGroup
import com.github.ai.kpdiff.domain.diff.formatter.TerminalOutputFormatter.Color
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DiffEventExtensionsKtTest {

    @Test
    fun `sortOrder should return ordered values depend on event and entity`() {
        // arrange
        val events = listOf(
            newUpdateEvent(newGroup(1), newGroup(2)),
            newUpdateEvent(newEntry(1), newEntry(2)),
            newUpdateEvent(newField(1), newField(2)),

            newDeleteEvent(newGroup(1)),
            newDeleteEvent(newEntry(1)),
            newDeleteEvent(newField(1)),

            newInsertEvent(newGroup(1)),
            newInsertEvent(newEntry(1)),
            newInsertEvent(newField(1))
        )

        // act
        val values = events.map { it.sortOrder() }

        // assert
        values shouldBe values.sorted()
    }

    @Test
    fun `getColor should return color depend on event`() {
        newInsertEvent(newEntry(1)).getColor() shouldBe Color.GREEN
        newDeleteEvent(newEntry(1)).getColor() shouldBe Color.RED
        newUpdateEvent(newEntry(1), newEntry(2)).getColor() shouldBe Color.YELLOW
    }

    @Test
    fun `chooseSourceByEventType should return left or right value depend on event type`() {
        newInsertEvent(newEntry(1)).chooseSourceByEventType(LEFT, RIGHT) shouldBe RIGHT
        newDeleteEvent(newEntry(1)).chooseSourceByEventType(LEFT, RIGHT) shouldBe LEFT

        newUpdateEvent(newEntry(1), newEntry(2)).chooseSourceByEventType(
            LEFT,
            RIGHT
        ) shouldBe RIGHT
    }

    private fun newInsertEvent(entity: DatabaseEntity): DiffEvent.Insert<DatabaseEntity> =
        DiffEvent.Insert(
            parentUuid = UUID_PARENT,
            entity = entity
        )

    private fun newDeleteEvent(entity: DatabaseEntity): DiffEvent.Delete<DatabaseEntity> =
        DiffEvent.Delete(
            parentUuid = UUID_PARENT,
            entity = entity
        )

    private fun newUpdateEvent(
        oldEntity: DatabaseEntity,
        newEntity: DatabaseEntity
    ): DiffEvent.Update<DatabaseEntity> =
        DiffEvent.Update(
            oldParentUuid = UUID_CHILD,
            newParentUuid = UUID_PARENT,
            oldEntity = oldEntity,
            newEntity = newEntity
        )

    companion object {
        private const val LEFT = "left"
        private const val RIGHT = "right"
    }
}