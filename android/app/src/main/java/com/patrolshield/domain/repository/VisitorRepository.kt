package com.patrolshield.domain.repository

import com.patrolshield.data.remote.dto.VisitorDto
import kotlinx.coroutines.flow.Flow

interface VisitorRepository {
    suspend fun getVisitorsToday(): Result<List<VisitorDto>>
    suspend fun checkInVisitor(id: Int): Result<VisitorDto>
}
