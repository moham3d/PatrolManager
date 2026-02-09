package com.patrolshield.data.remote.dto

data class IncidentRequest(
    val type: String,
    val priority: String,
    val description: String,
    val runId: Int?, // Optional linking to patrol run
    val siteId: Int, // Required by backend
    val lat: Double?,
    val lng: Double?,
    val imageBase64: String? = null // For simplicity in MVP
)
