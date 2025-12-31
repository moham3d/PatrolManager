package com.patrolshield.domain.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Flow<Resource<UserEntity>>
    suspend fun getUser(): UserEntity?
    suspend fun logout()
}
