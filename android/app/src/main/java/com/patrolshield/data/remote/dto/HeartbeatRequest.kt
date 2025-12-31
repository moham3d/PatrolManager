package com.patrolshield.data.remote.dto

data class HeartbeatRequest(
    val lat: Double,
    val lng: Double,
    val activeRunId: Int?
)
