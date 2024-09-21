package com.github.ai.kpdiff.entity

import java.security.MessageDigest
import java.util.Base64

@JvmInline
value class Hash(val value: String) {

    companion object {

        private const val SHA_256 = "SHA-256"

        fun fromContentBytes(bytes: ByteArray): Hash {
            val digest = MessageDigest.getInstance(SHA_256)
            val hash = digest.digest(bytes)
            return Hash(
                value = Base64.getEncoder().encodeToString(hash)
            )
        }
    }
}