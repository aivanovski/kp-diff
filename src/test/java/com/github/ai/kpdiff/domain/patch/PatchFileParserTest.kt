package com.github.ai.kpdiff.domain.patch

import arrow.core.Either
import com.github.ai.kpdiff.domain.patch.model.EntryEntity
import com.github.ai.kpdiff.domain.patch.model.EntryReference
import com.github.ai.kpdiff.domain.patch.model.Field
import com.github.ai.kpdiff.domain.patch.model.GroupEntity
import com.github.ai.kpdiff.domain.patch.model.GroupReference
import com.github.ai.kpdiff.domain.patch.model.PatchEvent
import com.github.ai.kpdiff.utils.Fields.FIELD_PASSWORD
import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import com.github.ai.kpdiff.utils.Fields.FIELD_USERNAME
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PatchFileParserTest {

    @Test
    fun `parse should handle entry insertion`() {
        // arrange
        val content = """
            ~ Group 'Group A'
            +     Entry 'Entry 1'
            +         Field '$FIELD_TITLE': 'Entry 1'
            +         Field '$FIELD_USERNAME': 'John.Doe'
            +         Field '$FIELD_PASSWORD': 'abc123'
        """.trimIndent()

        // act
        val result = PatchFileParser().parse(content)

        // assert
        result shouldBe Either.Right(
            listOf(
                PatchEvent.Insert(
                    parents = listOf(
                        GroupReference("Group A")
                    ),
                    entity = EntryEntity(
                        name = "Entry 1",
                        fields = mapOf(
                            FIELD_TITLE to "Entry 1",
                            FIELD_USERNAME to "John.Doe",
                            FIELD_PASSWORD to "abc123"
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `parse should handle entry deletion`() {
        // arrange
        val content = """
            ~ Group 'Group A'
            -     Entry 'Entry 1'
            -         Field '$FIELD_TITLE': 'Entry 1'
            -         Field '$FIELD_USERNAME': 'John.Doe'
            -         Field '$FIELD_PASSWORD': 'abc123'
        """.trimIndent()

        // act
        val result = PatchFileParser().parse(content)

        // assert
        result shouldBe Either.Right(
            listOf(
                PatchEvent.Delete(
                    parents = listOf(
                        GroupReference("Group A")
                    ),
                    entity = EntryEntity(
                        name = "Entry 1",
                        fields = mapOf(
                            FIELD_TITLE to "Entry 1",
                            FIELD_USERNAME to "John.Doe",
                            FIELD_PASSWORD to "abc123"
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `parse should handle entry deletion and insertion`() {
        // arrange
        val content = """
            ~ Group 'Group A'
            ~     Entry 'Entry 1'
            -         Field 'Field 1': 'Value 1'
            +         Field 'Field 2': 'Value 2'
        """.trimIndent()

        // act
        val result = PatchFileParser().parse(content)

        // assert
        val parents = listOf(
            GroupReference("Group A"),
            EntryReference("Entry 1")
        )

        result shouldBe Either.Right(
            listOf(
                PatchEvent.Delete(
                    parents = parents,
                    entity = Field(
                        name = "Field 1",
                        value = "Value 1"
                    )
                ),
                PatchEvent.Insert(
                    parents = parents,
                    entity = Field(
                        name = "Field 2",
                        value = "Value 2"
                    )
                )
            )
        )
    }

    @Test
    fun `parse should handle entry update`() {
        // arrange
        val content = """
            ~ Group 'Group A'
            ~     Entry 'Entry 1'
            ~         Field 'Field 1': 'Value 1' Changed to 'Value 2'
        """.trimIndent()

        // act
        val result = PatchFileParser().parse(content)

        // assert
        val parents = listOf(
            GroupReference("Group A"),
            EntryReference("Entry 1")
        )

        result shouldBe Either.Right(
            listOf(
                PatchEvent.Update(
                    parents = parents,
                    oldEntity = Field(
                        name = "Field 1",
                        value = "Value 1"
                    ),
                    newEntity = Field(
                        name = "Field 1",
                        value = "Value 2"
                    )
                )
            )
        )
    }

    @Test
    fun `parse should handle group insertion`() {
        // arrange
        val content = """
            ~ Group 'Group A'
            +     Group 'Group B'
        """.trimIndent()

        // act
        val result = PatchFileParser().parse(content)

        // assert
        result shouldBe Either.Right(
            listOf(
                PatchEvent.Insert(
                    parents = listOf(
                        GroupReference("Group A")
                    ),
                    entity = GroupEntity("Group B")
                )
            )
        )
    }

    @Test
    fun `parse should handle group deletion`() {
        // arrange
        val content = """
            ~ Group 'Group A'
            -     Group 'Group B'
        """.trimIndent()

        // act
        val result = PatchFileParser().parse(content)

        // assert
        result shouldBe Either.Right(
            listOf(
                PatchEvent.Delete(
                    parents = listOf(
                        GroupReference("Group A")
                    ),
                    entity = GroupEntity("Group B")
                )
            )
        )
    }

    @Test
    fun `parse should work with nested data`() {
        // arrange

        // act
        val result = PatchFileParser().parse(CONTENT)

        // assert
        result shouldBe Either.Right(
            listOf()
        )
    }

    companion object {

//        ~ Group 'Database'
//        ~     Group 'Internet'
//        ~         Entry 'Cloud keys'
//        -             Attachment 'old-key.ssh' 15 Bytes
//        +             Attachment 'new-key.ssh' 15 Bytes

//        ~ Group 'Database'
//        ~     Entry 'Entry 1'
//        -         Field 'custom-key-1': 'custom-value-1'
//        +         Field 'custom-key-2': 'custom-value-2'

        private val CONTENT = """
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
        """.trimIndent().trim()
    }
}