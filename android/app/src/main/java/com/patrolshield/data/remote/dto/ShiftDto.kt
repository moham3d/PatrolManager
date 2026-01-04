package com.patrolshield.data.remote.dto

data class ClockInRequest(
    val latitude: Double,
    val longitude: Double,
    val siteId: Int
)

data class ClockOutRequest(
    val latitude: Double,
    val longitude: Double
)

data class ShiftResponse(
    val id: Int,
    val startTime: String,
    val userId: Int,
    val siteId: Int?
)
