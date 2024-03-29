package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.TestData
import com.github.ai.kpdiff.domain.diff.differ.PathDatabaseDiffer
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.testUtils.open
import com.github.ai.kpdiff.utils.buildNodeTree
import com.github.aivanovski.keepasstreediff.PathDiffer
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DiffFormatterImplTest {

    @Test
    fun `format should return diff between databases`() {
        listOf(
            Pair(defaultOptions(), newPathDiffer()) to OUTPUT,
            Pair(verboseOptions(), newPathDiffer()) to VERBOSE_OUTPUT
        ).forEach { (data, expected) ->
            // arrange
            val (options, differ) = data
            val lhs = KeepassDatabase(
                root = TestData.DB_WITH_PASSWORD.open().content.group.buildNodeTree()
            )
            val rhs = KeepassDatabase(
                root = TestData.DB_WITH_PASSWORD_MODIFIED.open().content.group.buildNodeTree()
            )

            // act
            val diff = differ.getDiff(lhs, rhs)
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

    private fun newPathDiffer(): PathDatabaseDiffer =
        PathDatabaseDiffer(PathDiffer())

    private fun defaultOptions(): DiffFormatterOptions =
        DiffFormatterOptions(isColorEnabled = false, isVerboseOutput = false)

    private fun verboseOptions(): DiffFormatterOptions =
        DiffFormatterOptions(isColorEnabled = false, isVerboseOutput = true)

    companion object {
        private val OUTPUT = """
            ~ Group 'Root'
            +     Entry 'Entry 5'
            ~ Group 'Root'
            ~     Group 'Root group 1'
            -         Group 'Inner group 2'
            +         Group 'Inner group 3'
            ~ Group 'Root'
            ~     Group 'Root group 1'
            ~         Group 'Inner group 1'
            -             Entry 'Entry 3'
            ~ Group 'Root'
            ~     Group 'Root group 1'
            ~         Group 'Inner group 1'
            ~             Entry 'Entry 4 modified'
            ~                 Field 'Title': 'Entry 4' Changed to 'Entry 4 modified'

        """.transformOutput()

        private val VERBOSE_OUTPUT = """
            ~ Group 'Root'
            +     Entry 'Entry 5'
            +         Field 'Title': 'Entry 5'
            +         Field 'UserName': 'scott'
            +         Field 'Password': 'tiger'
            +         Field 'URL': ''
            +         Field 'Notes': ''
            ~ Group 'Root'
            ~     Group 'Root group 1'
            -         Group 'Inner group 2'
            +         Group 'Inner group 3'
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
            ~     Group 'Root group 1'
            ~         Group 'Inner group 1'
            ~             Entry 'Entry 4 modified'
            ~                 Field 'Title': 'Entry 4' Changed to 'Entry 4 modified'
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