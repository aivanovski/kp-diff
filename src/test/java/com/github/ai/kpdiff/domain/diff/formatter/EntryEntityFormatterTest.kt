package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.TestData.INDENT_DOUBLE
import com.github.ai.kpdiff.TestData.INDENT_EMPTY
import com.github.ai.kpdiff.TestData.INDENT_SINGLE
import com.github.ai.kpdiff.TestData.TITLE
import com.github.ai.kpdiff.TestData.TITLE1
import com.github.ai.kpdiff.TestData.TITLE2
import com.github.ai.kpdiff.TestData.UUID1
import com.github.ai.kpdiff.domain.diff.formatter.EntryEntityFormatter.Companion.ENTRY
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.EntryEntity.Companion.PROPERTY_TITLE
import com.github.ai.kpdiff.entity.SimpleNode
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class EntryEntityFormatterTest {

    @Test
    fun `format should return string for deletion`() {
        val entry = newEntity()

        newFormatter().format(
            DiffEvent.Delete(SimpleNode(entry.uuid, entry)),
            INDENT_EMPTY
        ) shouldBe "- $ENTRY '$TITLE'"
    }

    @Test
    fun `format should return string for insertion`() {
        val entry = newEntity()

        newFormatter().format(
            DiffEvent.Insert(SimpleNode(entry.uuid, entry)),
            INDENT_EMPTY
        ) shouldBe "+ $ENTRY '$TITLE'"
    }

    @Test
    fun `format should return string for update`() {
        val oldEntry = newEntity(TITLE1)
        val newEntry = newEntity(TITLE2)

        newFormatter().format(
            DiffEvent.Update(
                SimpleNode(oldEntry.uuid, oldEntry),
                SimpleNode(newEntry.uuid, newEntry)
            ),
            INDENT_EMPTY
        ) shouldBe "~ $ENTRY '$TITLE2'"
    }

    @Test
    fun `format should use correct indentation`() {
        val entry = newEntity()

        newFormatter().format(
            DiffEvent.Delete(SimpleNode(entry.uuid, entry)),
            INDENT_SINGLE
        ) shouldBe "-$INDENT_SINGLE $ENTRY '$TITLE'"

        newFormatter().format(
            DiffEvent.Delete(SimpleNode(entry.uuid, entry)),
            INDENT_DOUBLE
        ) shouldBe "-$INDENT_DOUBLE $ENTRY '$TITLE'"
    }

    private fun newFormatter(): EntryEntityFormatter =
        EntryEntityFormatter()

    private fun newEntity(title: String = TITLE): EntryEntity {
        return EntryEntity(
            UUID1,
            mapOf(PROPERTY_TITLE to title)
        )
    }
}