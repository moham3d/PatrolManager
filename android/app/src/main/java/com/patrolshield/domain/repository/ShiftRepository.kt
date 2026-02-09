package com.patrolshield.domain.repository

import com.patrolshield.data.remote.dto.ShiftDto

interface ShiftRepository {
    suspend fun clockIn(siteId: Int, lat: Double, lng: Double): Result<ShiftDto>
    suspend fun clockOut(lat: Double, lng: Double): Result<Unit>
}
