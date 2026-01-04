package com.patrolshield.data.repository

import com.patrolshield.common.Resource
import com.patrolshield.common.UserPreferences
import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.LoginRequest
import com.patrolshield.data.remote.dto.LoginResponse
import com.patrolshield.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userPrefs: UserPreferences
) : AuthRepository {

    override fun getAuthToken(): String? = userPrefs.getAuthToken()

    override fun isAuthenticated(): Boolean = getAuthToken() != null

    override suspend fun login(email: String, password: String): Resource<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                userPrefs.saveAuthToken(loginResponse.token)
                loginResponse.user.siteId?.let { userPrefs.saveSiteId(it) }
                Resource.Success(loginResponse)
            } else {
                Resource.Error("Login failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.localizedMessage}")
        }
    }

    override suspend fun logout() {
        userPrefs.clear()
    }
}
