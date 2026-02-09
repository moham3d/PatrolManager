package com.patrolshield.data.remote.dto

data class LivePatrolDto(
    val guardId: Int,
    val guardName: String,
    val lat: Double,
    val lng: Double,
    val lastSeen: Long, // Timestamp
    val status: String, // "active", "idle", "SOS"
    val patrolName: String? = null
)
