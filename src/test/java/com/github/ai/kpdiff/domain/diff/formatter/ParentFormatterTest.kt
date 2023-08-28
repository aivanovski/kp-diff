package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.TestData.INDENT_DOUBLE
import com.github.ai.kpdiff.TestData.INDENT_EMPTY
import com.github.ai.kpdiff.TestData.INDENT_SINGLE
import com.github.ai.kpdiff.TestData.TITLE
import com.github.ai.kpdiff.TestData.UUID1
import com.github.ai.kpdiff.domain.diff.formatter.ParentFormatter.Companion.ENTRY
import com.github.ai.kpdiff.domain.diff.formatter.ParentFormatter.Companion.GROUP
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.Parent
import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class ParentFormatterTest {

    @Test
    fun `format should format entry`() {
        val parent = newParent(EntryEntity(UUID1, mapOf(FIELD_TITLE to TITLE)))
        newFormatter().format(
            parent,
            INDENT_EMPTY
        ) shouldBe "~ $ENTRY '$TITLE'"
    }

    @Test
    fun `format should format group`() {
        val parent = newParent(GroupEntity(UUID1, TITLE))
        newFormatter().format(
            parent,
            INDENT_EMPTY
        ) shouldBe "~ $GROUP '$TITLE'"
    }

    @Test
    fun `format should throw IllegalArgumentException`() {
        val parent = newParent(null)
        shouldThrow<IllegalArgumentException> {
            newFormatter().format(
                parent,
                INDENT_EMPTY
            )
        }
    }

    @Test
    fun `format should use correct indentation`() {
        val parent = newParent(GroupEntity(UUID1, TITLE))

        newFormatter().format(
            parent,
            INDENT_SINGLE
        ) shouldBe "~$INDENT_SINGLE $GROUP '$TITLE'"

        newFormatter().format(
            parent,
            INDENT_DOUBLE
        ) shouldBe "~$INDENT_DOUBLE $GROUP '$TITLE'"
    }

    private fun newFormatter(): ParentFormatter {
        return ParentFormatter()
    }

    private fun newParent(entity: DatabaseEntity?): Parent {
        return Parent(
            entity = entity,
            uuid = entity?.uuid ?: UUID1
        )
    }
}