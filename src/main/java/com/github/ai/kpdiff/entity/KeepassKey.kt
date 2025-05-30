package com.github.ai.kpdiff.entity

sealed class KeepassKey {
    data class PasswordKey(val password: String) : KeepassKey()
    data class FileKey(val path: String) : KeepassKey()
    data class CompositeKey(val path: String, val password: String) : KeepassKey()
}