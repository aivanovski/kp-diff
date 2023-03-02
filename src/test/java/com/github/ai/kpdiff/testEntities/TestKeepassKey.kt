package com.github.ai.kpdiff.testEntities

sealed class TestKeepassKey {
    data class PasswordKey(val password: String) : TestKeepassKey()
}