package com.patrolshield.data.remote

import com.patrolshield.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("patrols/my-schedule")
    suspend fun getMySchedule(): Response<List<PatrolDto>>

    @POST("patrols/start")
    suspend fun startPatrol(@Body request: StartPatrolRequest): Response<StartPatrolResponse>

    @POST("patrols/scan")
    suspend fun scanCheckpoint(@Body request: ScanRequest): Response<Unit>

    @POST("patrols/end")
    suspend fun endPatrol(@Body request: EndPatrolRequest): Response<Unit>

    @POST("incidents/panic")
    suspend fun triggerPanic(@Body request: PanicRequest): Response<Unit>

    @POST("incidents")
    suspend fun reportIncident(@Body request: IncidentRequest): Response<Unit>

    @GET("supervisor/live-patrols")
    suspend fun getLivePatrols(): Response<List<LivePatrolDto>>

    @POST("patrols/heartbeat")
    suspend fun sendHeartbeat(@Body request: HeartbeatRequest): Response<Unit>

    @GET("manager/stats")
    suspend fun getManagerStats(): Response<ManagerStatsDto>
}
