package com.patrolshield.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val action: String,
    val payload: String,
    val priority: Int,
    val synced: Boolean = false,
    val timestamp: Long
)
