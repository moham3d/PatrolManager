package com.patrolshield.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val type: String,
    val priority: String, // low, medium, high, critical
    val description: String?,
    val localEvidencePath: String?,
    val status: String = "new",
    val lat: Double?,
    val lng: Double?,
    val timestamp: Long = System.currentTimeMillis()
)
