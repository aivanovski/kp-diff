package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.TestData.INDENT_DOUBLE
import com.github.ai.kpdiff.TestData.INDENT_EMPTY
import com.github.ai.kpdiff.TestData.INDENT_SINGLE
import com.github.ai.kpdiff.TestData.TITLE
import com.github.ai.kpdiff.TestData.TITLE1
import com.github.ai.kpdiff.TestData.TITLE2
import com.github.ai.kpdiff.TestData.UUID1
import com.github.ai.kpdiff.TestData.UUID_PARENT
import com.github.ai.kpdiff.domain.diff.formatter.EntryEntityFormatter.Companion.ENTRY
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class EntryEntityFormatterTest {

    @Test
    fun `format should return string for deletion`() {
        val entry = newEntity()

        newFormatter().format(
            DiffEvent.Delete(UUID_PARENT, entry),
            INDENT_EMPTY
        ) shouldBe "- $ENTRY '$TITLE'"
    }

    @Test
    fun `format should return string for insertion`() {
        val entry = newEntity()

        newFormatter().format(
            DiffEvent.Insert(UUID_PARENT, entry),
            INDENT_EMPTY
        ) shouldBe "+ $ENTRY '$TITLE'"
    }

    @Test
    fun `format should return string for update`() {
        val oldEntry = newEntity(TITLE1)
        val newEntry = newEntity(TITLE2)

        newFormatter().format(
            DiffEvent.Update(
                oldParentUuid = UUID_PARENT,
                newParentUuid = UUID_PARENT,
                oldEntity = oldEntry,
                newEntity = newEntry
            ),
            INDENT_EMPTY
        ) shouldBe "~ $ENTRY '$TITLE2'"
    }

    @Test
    fun `format should use correct indentation`() {
        val entry = newEntity()

        newFormatter().format(
            DiffEvent.Delete(UUID_PARENT, entry),
            INDENT_SINGLE
        ) shouldBe "-$INDENT_SINGLE $ENTRY '$TITLE'"

        newFormatter().format(
            DiffEvent.Delete(UUID_PARENT, entry),
            INDENT_DOUBLE
        ) shouldBe "-$INDENT_DOUBLE $ENTRY '$TITLE'"
    }

    private fun newFormatter(): EntryEntityFormatter = EntryEntityFormatter()

    private fun newEntity(title: String = TITLE): EntryEntity {
        return EntryEntity(
            UUID1,
            mapOf(FIELD_TITLE to title)
        )
    }
}