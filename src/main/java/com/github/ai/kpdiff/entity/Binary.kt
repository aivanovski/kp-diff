package com.github.ai.kpdiff.entity

data class Binary(
    val name: String,
    val hash: Hash,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Binary

        if (name != other.name) return false
        if (hash != other.hash) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + hash.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}