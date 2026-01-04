package com.patrolshield.data.remote.dto

data class PanicRequest(
    val lat: Double,
    val lng: Double,
    val patrolRunId: Int? = null
)

data class HeartbeatRequest(
    val lat: Double,
    val lng: Double,
    val patrolRunId: Int? = null,
    val batteryLevel: Int? = null
)
