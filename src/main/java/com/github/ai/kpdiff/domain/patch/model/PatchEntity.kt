package com.github.ai.kpdiff.domain.patch.model

sealed interface PatchEntity

data class GroupEntity(
    val name: String
) : PatchEntity

data class EntryEntity(
    val name: String,
    val fields: Map<String, String>
) : PatchEntity

data class Field(
    val name: String,
    val value: String
) : PatchEntity