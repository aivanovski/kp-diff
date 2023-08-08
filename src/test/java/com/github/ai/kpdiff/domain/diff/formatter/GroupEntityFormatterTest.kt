package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.TestData.INDENT_DOUBLE
import com.github.ai.kpdiff.TestData.INDENT_EMPTY
import com.github.ai.kpdiff.TestData.INDENT_SINGLE
import com.github.ai.kpdiff.TestData.TITLE1
import com.github.ai.kpdiff.TestData.TITLE2
import com.github.ai.kpdiff.TestData.UUID1
import com.github.ai.kpdiff.domain.diff.formatter.GroupEntityFormatter.Companion.GROUP
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.BasicNode
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class GroupEntityFormatterTest {

    @Test
    fun `format should return string for deletion`() {
        val group = newGroup()

        newFormatter().format(
            DiffEvent.Delete(BasicNode(group.uuid, group)),
            INDENT_EMPTY
        ) shouldBe "- $GROUP '$TITLE1'"
    }

    @Test
    fun `format should return string for insertion`() {
        val group = newGroup()

        newFormatter().format(
            DiffEvent.Insert(BasicNode(group.uuid, group)),
            INDENT_EMPTY
        ) shouldBe "+ $GROUP '$TITLE1'"
    }

    @Test
    fun `format should return string for update`() {
        val oldGroup = newGroup(TITLE1)
        val newGroup = newGroup(TITLE2)

        newFormatter().format(
            DiffEvent.Update(BasicNode(oldGroup.uuid, oldGroup), BasicNode(newGroup.uuid, newGroup)),
            INDENT_EMPTY
        ) shouldBe "~ $GROUP '$TITLE2'"
    }

    @Test
    fun `format should use correct indentation`() {
        val group = newGroup()

        newFormatter().format(
            DiffEvent.Delete(BasicNode(group.uuid, group)),
            INDENT_SINGLE
        ) shouldBe "-$INDENT_SINGLE $GROUP '$TITLE1'"

        newFormatter().format(
            DiffEvent.Delete(BasicNode(group.uuid, group)),
            INDENT_DOUBLE
        ) shouldBe "-$INDENT_DOUBLE $GROUP '$TITLE1'"
    }

    private fun newGroup(title: String = TITLE1): GroupEntity =
        GroupEntity(UUID1, title)

    private fun newFormatter(): GroupEntityFormatter =
        GroupEntityFormatter()
}