package com.patrolshield.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkpoints")
data class CheckpointEntity(
    @PrimaryKey
    val id: Int,
    val templateId: Int,
    val name: String,
    val lat: Double,
    val lng: Double,
    val isScanned: Boolean = false
)
