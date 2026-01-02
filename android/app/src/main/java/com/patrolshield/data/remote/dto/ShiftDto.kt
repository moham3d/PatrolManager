package com.patrolshield.data.remote.dto

data class ShiftDto(
    val id: Int,
    val userId: Int,
    val siteId: Int,
    val startTime: String,
    val endTime: String?,
    val status: String
)

data class ShiftResponse(
    val message: String,
    val shift: ShiftDto
)
