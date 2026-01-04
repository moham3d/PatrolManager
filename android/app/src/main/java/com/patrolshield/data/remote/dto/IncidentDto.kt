package com.patrolshield.data.remote.dto

data class IncidentRequest(
    val type: String,
    val priority: String,
    val description: String,
    val siteId: Int,
    val lat: Double?,
    val lng: Double?,
    val patrolRunId: Int? = null
)

data class Incident(
    val id: Int,
    val type: String,
    val priority: String,
    val status: String,
    val description: String?,
    val siteId: Int,
    val reporterId: Int,
    val lat: Double?,
    val lng: Double?,
    val createdAt: String,
    val evidence: List<IncidentEvidence>? = null
)

data class IncidentEvidence(
    val id: Int,
    val filePath: String,
    val type: String
)
