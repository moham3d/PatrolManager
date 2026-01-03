package com.patrolshield.data.remote.dto

data class ActiveIncidentsDto(
    val incidents: List<ActiveIncidentDto>,
    val panics: List<PanicDto>
)

data class ActiveIncidentDto(
    val id: Int,
    val type: String,
    val priority: String,
    val description: String?,
    val siteId: Int,
    val status: String,
    val createdAt: String // ISO Date string
)

data class PanicDto(
    val id: Int,
    val guardId: Int,
    val triggeredAt: String,
    val resolved: Boolean,
    val location: LocationDto?
)
