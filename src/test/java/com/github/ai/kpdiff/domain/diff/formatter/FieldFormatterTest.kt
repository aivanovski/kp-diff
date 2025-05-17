package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.TestData.INDENT_DOUBLE
import com.github.ai.kpdiff.TestData.INDENT_EMPTY
import com.github.ai.kpdiff.TestData.INDENT_SINGLE
import com.github.ai.kpdiff.TestData.UUID_CHILD
import com.github.ai.kpdiff.TestData.UUID_PARENT
import com.github.ai.kpdiff.TestData.VALUE1
import com.github.ai.kpdiff.TestData.VALUE2
import com.github.ai.kpdiff.domain.diff.formatter.FieldFormatter.Companion.ATTACHMENT
import com.github.ai.kpdiff.domain.diff.formatter.FieldFormatter.Companion.CHANGED_TO
import com.github.ai.kpdiff.domain.diff.formatter.FieldFormatter.Companion.FIELD
import com.github.ai.kpdiff.domain.usecases.FormatFileSizeUseCase
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.Field
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class FieldFormatterTest {

    @Test
    fun `format should format deleted text field`() {
        val field = newStringField()

        newFormatter().format(
            DiffEvent.Delete(UUID_PARENT, field),
            INDENT_EMPTY
        ) shouldBe "- $FIELD '${field.name}': '${field.value}'"
    }

    @Test
    fun `format should format inserted text field`() {
        val field = newStringField()

        newFormatter().format(
            DiffEvent.Insert(UUID_PARENT, field),
            INDENT_EMPTY
        ) shouldBe "+ $FIELD '${field.name}': '${field.value}'"
    }

    @Test
    fun `format should format updated text field`() {
        val old = newStringField(value = VALUE1)
        val new = newStringField(value = VALUE2)

        newFormatter().format(
            DiffEvent.Update(
                oldParentUuid = UUID_PARENT,
                newParentUuid = UUID_PARENT,
                oldEntity = old,
                newEntity = new
            ),
            INDENT_EMPTY
        ) shouldBe "~ $FIELD '${old.name}': '${old.value}' $CHANGED_TO '${new.value}'"
    }

    @Test
    fun `format should format deleted binary field`() {
        val field = newBinaryField()

        newFormatter().format(
            DiffEvent.Delete(UUID_PARENT, field),
            INDENT_EMPTY
        ) shouldBe "- $ATTACHMENT '${field.name}' ${field.value.size} Bytes"
    }

    @Test
    fun `format should format inserted binary field`() {
        val field = newBinaryField()

        newFormatter().format(
            DiffEvent.Insert(UUID_PARENT, field),
            INDENT_EMPTY
        ) shouldBe "+ $ATTACHMENT '${field.name}' ${field.value.size} Bytes"
    }

    @Test
    fun `format should use correct indent`() {
        val field = newStringField()

        newFormatter().format(
            DiffEvent.Delete(UUID_PARENT, field),
            INDENT_SINGLE
        ) shouldBe "-$INDENT_SINGLE $FIELD '${field.name}': '${field.value}'"

        newFormatter().format(
            DiffEvent.Delete(UUID_PARENT, field),
            INDENT_DOUBLE
        ) shouldBe "-$INDENT_DOUBLE $FIELD '${field.name}': '${field.value}'"
    }

    @Test
    fun `format should throw exception`() {
        shouldThrow<IllegalArgumentException> {
            val timestampField = Field(UUID_CHILD, FIELD_NAME, 1L)

            newFormatter().format(
                DiffEvent.Insert(UUID_PARENT, timestampField),
                INDENT_EMPTY
            )
        }
    }

    private fun newFormatter(): FieldFormatter {
        return FieldFormatter(formatFileSizeUseCase = FormatFileSizeUseCase())
    }

    private fun newBinaryField(
        name: String = BINARY_NAME,
        value: ByteArray = BINARY_VALUE
    ): Field<ByteArray> {
        return Field(UUID_CHILD, name, value)
    }

    private fun newStringField(
        name: String = FIELD_NAME,
        value: String = FIELD_VALUE
    ): Field<String> {
        return Field(UUID_CHILD, name, value)
    }

    companion object {
        private const val FIELD_NAME = "field-name"
        private const val FIELD_VALUE = "field-value"
        private const val BINARY_NAME = "binary-file.txt"
        private val BINARY_VALUE = "Binary value".toByteArray()
    }
}