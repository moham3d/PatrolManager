package com.patrolshield.data.remote.dto

data class PanicRequest(
    val location: LocationDto? = null,
    val patrolRunId: Int? = null
)


