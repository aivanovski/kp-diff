package com.github.ai.kpdiff.domain.diff

import com.github.ai.kpdiff.entity.DifferType
import com.github.ai.kpdiff.domain.diff.pathDiffer.PathDatabaseDiffer
import com.github.ai.kpdiff.domain.diff.uuidDiffer.UuidDatabaseDiffer
import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
import org.junit.jupiter.api.Test

class DatabaseDifferProviderTest {

    @Test
    fun `getDiffer should return correct differ`() {
        newProvider().getDiffer(DifferType.UUID) should beInstanceOf<UuidDatabaseDiffer>()
        newProvider().getDiffer(DifferType.PATH) should beInstanceOf<PathDatabaseDiffer>()
    }

    private fun newProvider(): DatabaseDifferProvider =
        DatabaseDifferProvider()
}