package com.patrolshield.data.remote.dto

data class ManagerStatsDto(
    val patrolsToday: Int,
    val incidentsToday: Int,
    val complianceRate: Int,
    val recentIncidents: List<ManagerIncidentDto>
)

data class ManagerIncidentDto(
    val id: Int,
    val type: String,
    val description: String,
    val status: String,
    val createdAt: String,
    val reporter: ReporterDto
)

data class ReporterDto(
    val name: String
)
