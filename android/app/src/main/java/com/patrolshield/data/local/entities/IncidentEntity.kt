package com.patrolshield.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val apiId: Int? = null,
    val type: String,
    val priority: String,
    val description: String,
    val siteId: Int,
    val lat: Double?,
    val lng: Double?,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING" // "PENDING", "SYNCED"
)
