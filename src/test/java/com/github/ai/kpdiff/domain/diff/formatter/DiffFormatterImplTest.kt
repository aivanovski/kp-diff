package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.DatabaseFactory.PASSWORD_KEY
import com.github.ai.kpdiff.DatabaseFactory.createDatabase
import com.github.ai.kpdiff.DatabaseFactory.createModifiedDatabase
import com.github.ai.kpdiff.domain.diff.differ.PathDatabaseDiffer
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.testUtils.buildNodeTree
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
                root = createDatabase(PASSWORD_KEY).buildNodeTree()
            )
            val rhs = KeepassDatabase(
                root = createModifiedDatabase(PASSWORD_KEY).buildNodeTree()
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

    private fun newPathDiffer(): PathDatabaseDiffer = PathDatabaseDiffer(PathDiffer())

    private fun defaultOptions(): DiffFormatterOptions =
        DiffFormatterOptions(isColorEnabled = false, isVerboseOutput = false)

    private fun verboseOptions(): DiffFormatterOptions =
        DiffFormatterOptions(isColorEnabled = false, isVerboseOutput = true)

    companion object {
        private val OUTPUT = """
            ~ Group 'Database'
            -     Group 'Email'
            ~ Group 'Database'
            ~     Group 'Internet'
            +         Group 'Shopping'
            ~ Group 'Database'
            ~     Group 'Internet'
            ~         Group 'Coding'
            -             Entry 'Github.com'
            +             Entry 'Gitlab'
            ~ Group 'Database'
            ~     Group 'Internet'
            ~         Entry 'Google'
            ~             Field 'Notes': '' Changed to 'https://google.com'
            ~ Group 'Database'
            ~     Group 'Internet'
            ~         Group 'Social'
            +             Entry 'Facebook'
        """.transformOutput()

        private val VERBOSE_OUTPUT = """
            ~ Group 'Database'
            -     Group 'Email'
            ~ Group 'Database'
            ~     Group 'Internet'
            +         Group 'Shopping'
            ~ Group 'Database'
            ~     Group 'Internet'
            ~         Group 'Coding'
            -             Entry 'Github.com'
            -                 Field 'Title': 'Github.com'
            -                 Field 'UserName': 'john.doe@example.com'
            -                 Field 'Password': 'abc123'
            -                 Field 'URL': 'https://github.com'
            -                 Field 'Notes': ''
            +             Entry 'Gitlab'
            +                 Field 'Title': 'Gitlab'
            +                 Field 'UserName': 'john.doe@example.com'
            +                 Field 'Password': 'abc123'
            +                 Field 'URL': 'https://gitlab.com'
            +                 Field 'Notes': ''
            ~ Group 'Database'
            ~     Group 'Internet'
            ~         Entry 'Google'
            ~             Field 'Notes': '' Changed to 'https://google.com'
            ~ Group 'Database'
            ~     Group 'Internet'
            ~         Group 'Social'
            +             Entry 'Facebook'
            +                 Field 'Title': 'Facebook'
            +                 Field 'UserName': 'john.doe@example.com'
            +                 Field 'Password': 'abc123'
            +                 Field 'URL': 'https://facebook.com'
            +                 Field 'Notes': ''
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