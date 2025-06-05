package com.github.ai.kpdiff.domain.patch.model

sealed interface EntityReference

data class GroupReference(
    val name: String
) : EntityReference

data class EntryReference(
    val name: String
) : EntityReference