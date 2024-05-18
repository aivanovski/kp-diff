package com.github.ai.kpdiff

import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.models.DatabaseElement
import com.github.aivanovski.keepasstreebuilder.DatabaseBuilderDsl
import com.github.aivanovski.keepasstreebuilder.converter.kotpass.KotpassDatabaseConverter
import com.github.aivanovski.keepasstreebuilder.model.Database
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey.PasswordKey
import com.github.ai.kpdiff.TestData.ENTRY_APPLE
import com.github.ai.kpdiff.TestData.ENTRY_FACEBOOK
import com.github.ai.kpdiff.TestData.ENTRY_GITHUB
import com.github.ai.kpdiff.TestData.ENTRY_GITLAB
import com.github.ai.kpdiff.TestData.ENTRY_GOOGLE
import com.github.ai.kpdiff.TestData.ENTRY_GOOGLE_MODIFIED
import com.github.ai.kpdiff.TestData.ENTRY_LAPTOP_LOGIN
import com.github.ai.kpdiff.TestData.ENTRY_LEETCODE
import com.github.ai.kpdiff.TestData.ENTRY_MICROSOFT
import com.github.ai.kpdiff.TestData.ENTRY_NAS_LOGIN
import com.github.ai.kpdiff.TestData.ENTRY_NEETCODE
import com.github.ai.kpdiff.TestData.GROUP_CODING
import com.github.ai.kpdiff.TestData.GROUP_EMAIL
import com.github.ai.kpdiff.TestData.GROUP_INTERNET
import com.github.ai.kpdiff.TestData.GROUP_ROOT
import com.github.ai.kpdiff.TestData.GROUP_SHOPPING
import com.github.ai.kpdiff.TestData.GROUP_SOCIAL
import com.github.ai.kpdiff.testUtils.toBuilderEntity

object DatabaseFactory {

    private const val DEFAULT_PASSWORD = "abc123"
    private const val DEFAULT_KEY_FILE_CONTENT = "abcdefghij1234567890"

    val PASSWORD_KEY = PasswordKey(DEFAULT_PASSWORD)
    val FILE_KEY = DatabaseKey.BinaryKey(DEFAULT_KEY_FILE_CONTENT.toByteArray())

    fun createDatabase(
        key: DatabaseKey = PASSWORD_KEY
    ): Database<DatabaseElement, KeePassDatabase> {
        return DatabaseBuilderDsl.newBuilder(KotpassDatabaseConverter())
            .key(key)
            .content(GROUP_ROOT.toBuilderEntity()) {
                group(GROUP_EMAIL.toBuilderEntity())
                group(GROUP_INTERNET.toBuilderEntity()) {
                    group(GROUP_CODING.toBuilderEntity()) {
                        entry(ENTRY_LEETCODE.toBuilderEntity())
                        entry(ENTRY_NEETCODE.toBuilderEntity())
                        entry(ENTRY_GITHUB.toBuilderEntity())
                    }
                    group(GROUP_SOCIAL.toBuilderEntity())

                    entry(ENTRY_GOOGLE.toBuilderEntity())
                    entry(ENTRY_APPLE.toBuilderEntity())
                    entry(ENTRY_MICROSOFT.toBuilderEntity())
                }
                entry(ENTRY_NAS_LOGIN.toBuilderEntity())
                entry(ENTRY_LAPTOP_LOGIN.toBuilderEntity())
            }
            .build()
    }

    fun createModifiedDatabase(key: DatabaseKey): Database<DatabaseElement, KeePassDatabase> {
        return DatabaseBuilderDsl.newBuilder(KotpassDatabaseConverter())
            .key(key)
            .content(GROUP_ROOT.toBuilderEntity()) {
                group(GROUP_INTERNET.toBuilderEntity()) {
                    group(GROUP_CODING.toBuilderEntity()) {
                        entry(ENTRY_LEETCODE.toBuilderEntity())
                        entry(ENTRY_NEETCODE.toBuilderEntity())
                        entry(ENTRY_GITLAB.toBuilderEntity())
                    }
                    group(GROUP_SOCIAL.toBuilderEntity()) {
                        entry(ENTRY_FACEBOOK.toBuilderEntity())
                    }
                    group(GROUP_SHOPPING.toBuilderEntity())

                    entry(ENTRY_GOOGLE_MODIFIED.toBuilderEntity())
                    entry(ENTRY_APPLE.toBuilderEntity())
                    entry(ENTRY_MICROSOFT.toBuilderEntity())
                }
                entry(ENTRY_NAS_LOGIN.toBuilderEntity())
                entry(ENTRY_LAPTOP_LOGIN.toBuilderEntity())
            }
            .build()
    }
}