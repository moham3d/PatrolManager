package com.patrolshield.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_logs")
data class SyncLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "INCIDENT", "SCAN", "SOS", "HEARTBEAT"
    val payload: String, // JSON string
    val timestamp: Long = System.currentTimeMillis()
)
