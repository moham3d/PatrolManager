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
