package com.patrolshield.data.remote.dto

data class CheckpointDto(
    val id: Int,
    val name: String,
    val description: String?,
    val lat: Double,
    val lng: Double,
    val radius: Int = 20
)
