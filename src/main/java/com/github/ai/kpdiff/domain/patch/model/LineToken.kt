package com.github.ai.kpdiff.domain.patch.model

data class LineToken(
    val type: TokenType,
    val entityType: EntityType,
    val value: TokenValue,
    val line: TextLine
)

enum class TokenType {
    INSERT,
    DELETE,
    UPDATE
}

enum class EntityType {
    GROUP,
    ENTRY,
    FIELD,
    // TODO: implement attachements
    ATTACHMENT
}

sealed interface TokenValue {

    data class Name(
        val value: String
    ) : TokenValue

    data class NameAndValue(
        val name: String,
        val value: String
    ) : TokenValue

    data class UpdateValue(
        val name: String,
        val oldValue: String,
        val newValue: String
    ) : TokenValue
}
