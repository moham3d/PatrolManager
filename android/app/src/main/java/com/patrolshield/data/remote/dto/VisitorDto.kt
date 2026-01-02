package com.patrolshield.data.remote.dto

data class VisitorDto(
    val id: Int,
    val name: String,
    val purpose: String,
    val siteId: Int,
    val hostName: String,
    val expectedArrivalTime: String,
    val status: String, // 'expected', 'checked_in', 'checked_out'
    val site: SiteDto? = null
)
