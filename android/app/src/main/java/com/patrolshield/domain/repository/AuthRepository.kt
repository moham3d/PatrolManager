package com.patrolshield.domain.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.remote.dto.LoginResponse
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<LoginResponse>
    suspend fun logout()
    fun getAuthToken(): String?
    fun isAuthenticated(): Boolean
}
