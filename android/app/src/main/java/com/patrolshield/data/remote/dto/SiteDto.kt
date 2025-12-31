package com.patrolshield.data.remote.dto

data class SiteDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    val lat: Double,
    val lng: Double,
    val radius: Int = 50
)
