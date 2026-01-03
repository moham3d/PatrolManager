package com.patrolshield.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String, // SCAN, GPS, INCIDENT, PANIC
    val payload: String, // JSON payload to send to server
    val priority: Int = 3, // 1: Critical (Panic), 2: High (Scan), 3: Medium (Incident/Shift), 4: Low (GPS)
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    val retryCount: Int = 0
)
