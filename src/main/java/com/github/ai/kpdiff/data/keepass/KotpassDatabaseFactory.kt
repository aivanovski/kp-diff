package com.github.ai.kpdiff.data.keepass

import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.utils.buildNodeTree
import com.github.ai.kpdiff.utils.toCredentials
import io.github.anvell.kotpass.database.KeePassDatabase
import io.github.anvell.kotpass.database.decode

class KotpassDatabaseFactory(
    private val fsProvider: FileSystemProvider
) : KeepassDatabaseFactory {

    override fun createDatabase(path: String, key: KeepassKey): Either<KeepassDatabase> {
        val content = fsProvider.open(path)
        if (content.isLeft()) {
            return content.mapToLeft()
        }

        return try {
            val creds = key.toCredentials()
            val db = KeePassDatabase.decode(content.unwrap(), creds)
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