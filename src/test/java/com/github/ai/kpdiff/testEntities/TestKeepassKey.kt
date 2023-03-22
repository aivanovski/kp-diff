package com.github.ai.kpdiff.testEntities

sealed class TestKeepassKey {
    data class PasswordKey(val password: String) : TestKeepassKey()
    data class FileKey(val path: String) : TestKeepassKey()
}