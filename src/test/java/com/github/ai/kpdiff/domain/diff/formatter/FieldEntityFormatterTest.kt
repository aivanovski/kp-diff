package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.TestData.INDENT_DOUBLE
import com.github.ai.kpdiff.TestData.INDENT_EMPTY
import com.github.ai.kpdiff.TestData.INDENT_SINGLE
import com.github.ai.kpdiff.TestData.NAME
import com.github.ai.kpdiff.TestData.UUID_CHILD
import com.github.ai.kpdiff.TestData.UUID_PARENT
import com.github.ai.kpdiff.TestData.VALUE
import com.github.ai.kpdiff.TestData.VALUE1
import com.github.ai.kpdiff.TestData.VALUE2
import com.github.ai.kpdiff.domain.diff.formatter.FieldEntityFormatter.Companion.CHANGED_TO
import com.github.ai.kpdiff.domain.diff.formatter.FieldEntityFormatter.Companion.FIELD
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.BasicNode
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class FieldEntityFormatterTest {

    @Test
    fun `format should return string for deletion`() {
        val entity = newEntity()

        newFormatter().format(
            DiffEvent.Delete(BasicNode(entity.uuid, entity)),
            INDENT_EMPTY
        ) shouldBe "- $FIELD '$NAME': '$VALUE'"
    }

    @Test
    fun `format should return string for insertion`() {
        val entity = newEntity()

        newFormatter().format(
            DiffEvent.Insert(BasicNode(entity.uuid, entity)),
            INDENT_EMPTY
        ) shouldBe "+ $FIELD '$NAME': '$VALUE'"
    }

    @Test
    fun `format should return string for update`() {
        val oldEntity = newEntity(value = VALUE1)
        val newEntity = newEntity(value = VALUE2)

        newFormatter().format(
            DiffEvent.Update(BasicNode(oldEntity.uuid, oldEntity), BasicNode(newEntity.uuid, newEntity)),
            INDENT_EMPTY
        ) shouldBe "~ $FIELD '$NAME': '$VALUE1' $CHANGED_TO '$VALUE2'"
    }

    @Test
    fun `format should use correct indent`() {
        val entity = newEntity()

        newFormatter().format(
            DiffEvent.Delete(BasicNode(entity.uuid, entity)),
            INDENT_SINGLE
        ) shouldBe "-$INDENT_SINGLE $FIELD '$NAME': '$VALUE'"

        newFormatter().format(
            DiffEvent.Delete(BasicNode(entity.uuid, entity)),
            INDENT_DOUBLE
        ) shouldBe "-$INDENT_DOUBLE $FIELD '$NAME': '$VALUE'"
    }

    private fun newFormatter(): FieldEntityFormatter {
        return FieldEntityFormatter()
    }

    private fun newEntity(
        name: String = NAME,
        value: String = VALUE
    ): FieldEntity {
        return FieldEntity(UUID_CHILD, UUID_PARENT, name, value)
    }
}