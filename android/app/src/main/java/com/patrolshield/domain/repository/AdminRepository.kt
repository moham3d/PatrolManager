package com.patrolshield.domain.repository

import com.patrolshield.data.remote.dto.AdminStatsDto

interface AdminRepository {
    suspend fun getStats(): Result<AdminStatsDto>
}
