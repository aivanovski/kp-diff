package com.github.ai.kpdiff.data.keepass

import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.database.decode
import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.utils.buildNodeTree
import com.github.ai.kpdiff.utils.toCredentials

class KotpassDatabaseFactory(
    private val fsProvider: FileSystemProvider
) : KeepassDatabaseFactory {

    override fun createDatabase(
        path: String,
        key: KeepassKey
    ): Either<KeepassDatabase> {
        val creds = key.toCredentials(fsProvider)
        if (creds.isLeft()) {
            return creds.mapToLeft()
        }

        val content = fsProvider.openForRead(path)
        if (content.isLeft()) {
            return content.mapToLeft()
        }

        return try {
            val db = KeePassDatabase.decode(content.unwrap(), creds.unwrap())
            Either.Right(db.convert())
        } catch (exception: Exception) {
            Either.Left(exception)
        }
    }

    private fun KeePassDatabase.convert(): KeepassDatabase {
        return KeepassDatabase(
            root = content.group.buildNodeTree()
        )
    }
}