package com.github.ai.kpdiff.domain.diff

import com.github.ai.kpdiff.domain.diff.differ.PathDatabaseDiffer
import com.github.ai.kpdiff.domain.diff.differ.UuidDatabaseDiffer
import com.github.ai.kpdiff.entity.DifferType
import com.github.aivanovski.keepasstreediff.PathDiffer
import com.github.aivanovski.keepasstreediff.UuidDiffer

class DatabaseDifferProvider {

    fun getDiffer(type: DifferType): DatabaseDiffer {
        return when (type) {
            DifferType.PATH -> PathDatabaseDiffer(differ = PathDiffer())
            DifferType.UUID -> UuidDatabaseDiffer(differ = UuidDiffer())
        }
    }
}