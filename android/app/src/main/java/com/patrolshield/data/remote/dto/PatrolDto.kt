package com.patrolshield.data.remote.dto

data class PatrolDto(
    val id: Int,
    val name: String,
    val description: String?,
    val siteId: Int,
    val estimatedDurationMinutes: Int?,
    val checkpoints: List<CheckpointDto> = emptyList()
)

data class PatrolScheduleDto(
    val id: Int,
    val name: String,
    val description: String?,
    val estimatedDurationMinutes: Int,
    val Site: SiteDto,
    val checkpoints: List<CheckpointDto> = emptyList()
)

data class StartPatrolRequest(
    val templateId: Int
)

data class StartPatrolResponse(
    val message: String,
    val runId: Int
)

data class ScanRequest(
    val runId: Int,
    val checkpointId: Int,
    val location: LocationDto?
)

data class LocationDto(
    val lat: Double,
    val lng: Double
)

data class EndPatrolRequest(
    val runId: Int
)
