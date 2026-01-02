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

    @GET("visitors/today")
    suspend fun getVisitorsToday(): Response<List<VisitorDto>>

    @GET("sites")
    suspend fun getSites(): Response<SiteListResponse>

    @POST("visitors/{id}/check-in")
    suspend fun checkInVisitor(@retrofit2.http.Path("id") id: Int): Response<VisitorDto>

    @POST("shifts/clock-in")
    suspend fun clockIn(@Body request: ClockInRequest): Response<ShiftResponse>

    @POST("shifts/clock-out")
    suspend fun clockOut(@Body request: ClockOutRequest): Response<Unit>

    // Admin
    @GET("admin/stats")
    suspend fun getAdminStats(): Response<AdminStatsDto>

    @GET("admin/users")
    suspend fun getUsers(): Response<List<UserDto>>

    @POST("admin/users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<CreateUserResponse>

    @POST("sites")
    suspend fun createSite(@Body request: CreateSiteRequest): Response<SiteDto>
}
