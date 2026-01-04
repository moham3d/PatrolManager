package com.patrolshield.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patrols")
data class PatrolEntity(
    @PrimaryKey val id: Int, // API ID
    val name: String,
    val description: String?,
    val scheduledDate: Long,
    val startTime: Long?,
    val endTime: Long?,
    val status: String, // "scheduled", "started", "completed"
    val syncStatus: String = "SYNCED" // "SYNCED", "PENDING"
)

@Entity(tableName = "checkpoints")
data class CheckpointEntity(
    @PrimaryKey val id: Int, // API ID
    val patrolId: Int,
    val name: String,
    val type: String, // "QR", "NFC"
    val tagId: String,
    val latitude: Double,
    val longitude: Double,
    val isVisited: Boolean = false,
    val visitedAt: String? = null
)
