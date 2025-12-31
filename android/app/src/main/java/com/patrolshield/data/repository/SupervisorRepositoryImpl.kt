package com.patrolshield.data.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.LivePatrolDto
import com.patrolshield.domain.repository.SupervisorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SupervisorRepositoryImpl @Inject constructor(
    private val api: ApiService
) : SupervisorRepository {

    override suspend fun getLivePatrols(): Flow<Resource<List<LivePatrolDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getLivePatrols()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Unknown error"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}
