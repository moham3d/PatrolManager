package com.patrolshield.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class PatrolWithCheckpoints(
    @Embedded val patrol: PatrolEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "patrolId"
    )
    val checkpoints: List<CheckpointEntity>
)
