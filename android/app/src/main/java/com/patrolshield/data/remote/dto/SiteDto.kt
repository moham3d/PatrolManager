package com.patrolshield.data.remote.dto

data class SiteDto(
    val id: Int,
    val name: String,
    val address: String?,
    val lat: Double?,
    val lng: Double?
)

data class SiteListResponse(
    val title: String,
    val sites: List<SiteDto>
)
