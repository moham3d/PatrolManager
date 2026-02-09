package com.patrolshield.data.repository

import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.CreateUserRequest
import com.patrolshield.data.remote.dto.UserDto
import com.patrolshield.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: ApiService
) : UserRepository {

    override suspend fun getUsers(): Result<List<UserDto>> {
        return try {
            val response = api.getUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch users: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUser(name: String, email: String, password: String, roleId: Int): Result<UserDto> {
        return try {
            val request = CreateUserRequest(name, email, password, roleId)
            val response = api.createUser(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.user)
            } else {
                Result.failure(Exception("Failed to create user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
