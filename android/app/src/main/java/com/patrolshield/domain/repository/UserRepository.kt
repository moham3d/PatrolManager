package com.patrolshield.domain.repository

import com.patrolshield.data.remote.dto.CreateUserRequest
import com.patrolshield.data.remote.dto.UserDto

interface UserRepository {
    suspend fun getUsers(): Result<List<UserDto>>
    suspend fun createUser(name: String, email: String, password: String, roleId: Int): Result<UserDto>
}
