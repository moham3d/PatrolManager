package com.patrolshield.data.remote.dto

data class ClockInRequest(
    val siteId: Int,
    val latitude: Double,
    val longitude: Double
)

data class ClockOutRequest(
    val latitude: Double,
    val longitude: Double
)
