package com.patrolshield.data.remote

import com.patrolshield.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("incidents/panic")
    suspend fun triggerPanic(@Body request: PanicRequest): Response<Unit>

    @POST("patrols/heartbeat")
    suspend fun sendHeartbeat(@Body request: HeartbeatRequest): Response<Unit>

    @POST("shifts/clock-in")
    suspend fun clockIn(@Body request: ClockInRequest): Response<ShiftResponse>

    @POST("shifts/clock-out")
    suspend fun clockOut(@Body request: ClockOutRequest): Response<Unit>

    @GET("patrols/my-schedule")
    suspend fun getMySchedule(): Response<List<PatrolDto>>

    @POST("patrols/start")
    suspend fun startPatrol(@Body request: StartPatrolRequest): Response<Int> // Returns patrolRunId

    @POST("patrols/scan")
    suspend fun scanCheckpoint(@Body request: ScanRequest): Response<Unit>

    @POST("patrols/end")
    suspend fun endPatrol(@Query("runId") runId: Int): Response<Unit>

    @POST("incidents")
    suspend fun reportIncident(@Body request: IncidentRequest): Response<Unit>

    @Multipart
    @POST("incidents/upload")
    suspend fun reportIncidentMultipart(
        @Part("type") type: okhttp3.RequestBody,
        @Part("priority") priority: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody,
        @Part("siteId") siteId: okhttp3.RequestBody,
        @Part("lat") lat: okhttp3.RequestBody?,
        @Part("lng") lng: okhttp3.RequestBody?,
        @Part("patrolRunId") patrolRunId: okhttp3.RequestBody?,
        @Part evidence: okhttp3.MultipartBody.Part?
    ): Response<Unit>
    
    // Additional endpoints will be added as we implement specific features
    @Multipart
    @POST("incidents/api/{id}/resolve")
    suspend fun resolveIncident(
        @Path("id") incidentId: Int,
        @Part("comment") comment: okhttp3.RequestBody,
        @Part evidence: okhttp3.MultipartBody.Part?
    ): Response<Unit>
}
