package com.patrolshield.data.remote.dto

data class PatrolDto(
    val id: Int,
    val name: String,
    val siteId: Int,
    val checkpoints: List<CheckpointDto>
)

data class CheckpointDto(
    val id: Int,
    val name: String,
    val type: String, // "QR" or "NFC"
    val value: String, // The actual QR content or NFC ID
    val order: Int
)

data class StartPatrolRequest(
    val patrolId: Int,
    val startTime: Long = System.currentTimeMillis()
)

data class ScanRequest(
    val patrolRunId: Int,
    val checkpointId: Int,
    val value: String,
    val lat: Double?,
    val lng: Double?,
    val timestamp: Long = System.currentTimeMillis()
)
