package com.github.ai.kpdiff.data.keepass

import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.KeepassKey

interface KeepassDatabaseFactory {
    fun createDatabase(path: String, key: KeepassKey): Either<KeepassDatabase>
}