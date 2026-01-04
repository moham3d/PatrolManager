package com.patrolshield.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_shift")
data class ShiftEntity(
    @PrimaryKey val id: Int,
    val startTime: Long,
    val siteId: Int?,
    val isActive: Boolean = true
)
