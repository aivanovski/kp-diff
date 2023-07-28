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
        listOf(
            DiffFormatterOptions(isColorEnabled = false, isVerboseOutput = false) to OUTPUT,
            DiffFormatterOptions(isColorEnabled = false, isVerboseOutput = true) to VERBOSE_OUTPUT
        ).forEach { (options, expected) ->
            // arrange
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
                diff = diff,
                options = options
            )

            // assert
            result shouldBe expected
        }
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
        """.transformOutput()

        private val VERBOSE_OUTPUT = """
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
            -                 Field 'Title': 'Entry 3'
            -                 Field 'UserName': 'scott'
            -                 Field 'Password': 'tiger'
            -                 Field 'URL': ''
            -                 Field 'Notes': ''
            ~ Group 'Root'
            +     Entry 'Entry 5'
            +         Field 'Title': 'Entry 5'
            +         Field 'UserName': 'scott'
            +         Field 'Password': 'tiger'
            +         Field 'URL': ''
            +         Field 'Notes': ''
        """.transformOutput()

        private fun String.transformOutput(): List<String> {
            return this
                .trimIndent()
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }
    }
}