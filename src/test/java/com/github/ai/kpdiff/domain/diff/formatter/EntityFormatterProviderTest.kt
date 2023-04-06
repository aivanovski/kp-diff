package com.github.ai.kpdiff.domain.diff.formatter

import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.GroupEntity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
import org.junit.jupiter.api.Test

internal class EntityFormatterProviderTest {

    @Test
    fun `getFormatter should return correct formatter`() {
        newProvider().getFormatter(GroupEntity::class) should beInstanceOf<GroupEntityFormatter>()
        newProvider().getFormatter(EntryEntity::class) should beInstanceOf<EntryEntityFormatter>()
        newProvider().getFormatter(FieldEntity::class) should beInstanceOf<FieldEntityFormatter>()
    }

    @Test
    fun `getFormatter should throw IllegalArgumentException`() {
        shouldThrow<IllegalArgumentException> {
            newProvider().getFormatter(DatabaseEntity::class)
        }
    }

    private fun newProvider(): EntityFormatterProvider =
        EntityFormatterProvider()
}