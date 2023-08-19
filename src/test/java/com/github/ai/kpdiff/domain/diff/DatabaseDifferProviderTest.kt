package com.github.ai.kpdiff.domain.diff

import com.github.ai.kpdiff.domain.diff.differ.PathDatabaseDiffer
import com.github.ai.kpdiff.domain.diff.differ.UuidDatabaseDiffer
import com.github.ai.kpdiff.entity.DifferType
import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
import org.junit.jupiter.api.Test

class DatabaseDifferProviderTest {

    @Test
    fun `getDiffer should return correct differ`() {
        newProvider().getDiffer(DifferType.PATH) should beInstanceOf<PathDatabaseDiffer>()
        newProvider().getDiffer(DifferType.UUID) should beInstanceOf<UuidDatabaseDiffer>()
    }

    private fun newProvider(): DatabaseDifferProvider =
        DatabaseDifferProvider()
}