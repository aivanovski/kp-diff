package com.github.ai.kpdiff.domain.diff

import com.github.ai.kpdiff.domain.diff.pathDiffer.PathDatabaseDiffer
import com.github.ai.kpdiff.domain.diff.uuidDiffer.UuidDatabaseDiffer
import com.github.ai.kpdiff.entity.DifferType

class DatabaseDifferProvider {

    fun getDiffer(type: DifferType): DatabaseDiffer {
        return when (type) {
            DifferType.UUID -> UuidDatabaseDiffer()
            DifferType.PATH -> PathDatabaseDiffer()
        }
    }
}