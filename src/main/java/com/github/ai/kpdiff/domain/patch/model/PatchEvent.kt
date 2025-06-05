package com.github.ai.kpdiff.domain.patch.model

sealed interface PatchEvent {

    data class Insert(
        val parents: List<EntityReference>,
        val entity: PatchEntity
    ) : PatchEvent

    data class Delete(
        val parents: List<EntityReference>,
        val entity: PatchEntity
    ) : PatchEvent

    data class Update(
        val parents: List<EntityReference>,
        val oldEntity: PatchEntity,
        val newEntity: PatchEntity
    ) : PatchEvent
}