package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.TestData
import com.github.ai.kpdiff.domain.diff.DatabaseDifferImpl
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.testUtils.open
import com.github.ai.kpdiff.utils.buildNodeTree
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DiffFormatterImplTest {

    @Test
    fun `format should return diff between databases`() {
        // arrange
        val expected = OUTPUT.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val lhs = KeepassDatabase(
            root = TestData.DB_WITH_PASSWORD.open().content.group.buildNodeTree()
        )
        val rhs = KeepassDatabase(
            root = TestData.DB_WITH_PASSWORD_MODIFIED.open().content.group.buildNodeTree()
        )

        // act
        val diff = DatabaseDifferImpl().getDiff(lhs, rhs)
        val result = DiffFormatterImpl(
            formatterProvider = EntityFormatterProvider(),
            parentFormatter = ParentFormatter(),
            terminalOutputFormatter = TerminalOutputFormatter()
        ).format(
            diff,
            options = DiffFormatterOptions(isColorEnabled = false)
        )

        // assert
        result shouldBe expected
    }

    companion object {
        private val OUTPUT = """
            ~ Group 'Root'
            ~     Group 'Root group 1'
            -         Group 'Inner group 2'
            +         Group 'Inner group 3'
            ~ Group 'Root'
            ~     Group 'Root group 1'
            ~         Group 'Inner group 1'
            ~             Entry 'Entry 4 modified'
            ~                 Field 'Title': 'Entry 4' Changed to 'Entry 4 modified'
            ~ Group 'Root'
            ~     Group 'Root group 1'
            ~         Group 'Inner group 1'
            -             Entry 'Entry 3'
            ~ Group 'Root'
            +     Entry 'Entry 5'
        """.trimIndent()
    }
}