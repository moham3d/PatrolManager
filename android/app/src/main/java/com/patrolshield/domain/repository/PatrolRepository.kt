package com.patrolshield.domain.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.local.entities.ShiftEntity
import com.patrolshield.data.remote.dto.*
import kotlinx.coroutines.flow.Flow

interface PatrolRepository {
    suspend fun clockIn(lat: Double, lng: Double, siteId: Int): Resource<ShiftResponse>
    suspend fun clockOut(lat: Double, lng: Double): Resource<Unit>
    fun getActiveShift(): Flow<ShiftEntity?>

    suspend fun getMySchedule(): Resource<List<PatrolDto>>
    suspend fun startPatrol(patrolId: Int): Resource<Int>
    suspend fun scanCheckpoint(runId: Int, checkpointId: Int, value: String, lat: Double?, lng: Double?): Resource<Unit>
    suspend fun endPatrol(runId: Int): Resource<Unit>
    suspend fun triggerPanic(lat: Double, lng: Double, runId: Int?): Resource<Unit>
}
