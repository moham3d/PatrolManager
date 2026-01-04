package com.patrolshield.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_logs")
data class SyncLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val action: String, // "START_PATROL", "SCAN_CHECKPOINT", "END_PATROL", "PANIC_ALERT"
    val payload: String, // JSON payload
    val priority: Int = 0, // 0 = Normal, 1 = High/Critical
    val timestamp: Long = System.currentTimeMillis()
)
