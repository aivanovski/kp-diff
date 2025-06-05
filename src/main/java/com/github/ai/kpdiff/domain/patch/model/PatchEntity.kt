package com.github.ai.kpdiff.domain.patch.model

import com.github.ai.kpdiff.entity.Binary

sealed interface PatchEntity

data class GroupEntity(
    val name: String
) : PatchEntity

data class EntryEntity(
    val name: String,
    val fields: Map<String, String>,
    val binaries: List<Binary> = emptyList()
) : PatchEntity

data class Field(
    val name: String,
    val value: String
) : PatchEntity