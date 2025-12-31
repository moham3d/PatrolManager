package com.patrolshield.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patrols")
data class PatrolEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null, // Null if created offline and not yet synced
    val templateId: Int = 0,
    val siteId: Int,
    val status: String, // started, completed, incomplete
    val startTime: Long,
    val endTime: Long? = null,
    val completionPercentage: Int = 0,
    val notes: String? = null
)
