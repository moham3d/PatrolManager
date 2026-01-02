package com.patrolshield.domain.repository

import com.patrolshield.data.remote.dto.SiteDto

interface SiteRepository {
    suspend fun getSites(): Result<List<SiteDto>>
    suspend fun createSite(name: String, address: String, lat: Double, lng: Double): Result<SiteDto>
}
