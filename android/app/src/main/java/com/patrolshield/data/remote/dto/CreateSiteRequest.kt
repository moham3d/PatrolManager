package com.patrolshield.data.remote.dto

data class CreateSiteRequest(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val radius: Int = 500
)
