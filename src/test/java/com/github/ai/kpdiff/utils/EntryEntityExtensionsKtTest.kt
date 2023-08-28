package com.github.ai.kpdiff.utils

import com.github.ai.kpdiff.TestData.TITLE
import com.github.ai.kpdiff.TestData.UUID1
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.utils.Fields.FIELD_TITLE
import com.github.ai.kpdiff.utils.StringUtils.EMPTY
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class EntryEntityExtensionsKtTest {

    @Test
    fun `getTitle should return value`() {
        val entry = EntryEntity(UUID1, mapOf(FIELD_TITLE to TITLE))
        entry.getTitle() shouldBe TITLE
    }

    @Test
    fun `getTitle should return empty value`() {
        val entry = EntryEntity(UUID1, mapOf())
        entry.getTitle() shouldBe EMPTY
    }
}