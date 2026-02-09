package com.patrolshield.data.remote.dto

data class AdminStatsDto(
    val totalUsers: Int,
    val activeUsers: Int,
    val roleCounts: Map<String, Int>
)
