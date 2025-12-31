package com.patrolshield.data.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.local.dao.UserDao
import com.patrolshield.data.local.entities.UserEntity
import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.LoginRequest
import com.patrolshield.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun login(email: String, password: String): Flow<Resource<UserEntity>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                
                // Save Token
                // TODO: use EncryptedSharedPreferences
                
                // Save User
                val userDto = loginResponse.user
                if (userDto != null) {
                    val user = UserEntity(
                        id = userDto.id,
                        name = userDto.name,
                        email = userDto.email,
                        role = userDto.Role?.name ?: when (userDto.roleId) {
                            1 -> "admin"
                            3 -> "supervisor" // Fallback based on seeder order
                            else -> "guard"
                        }, 
                        token = loginResponse.token
                    )
                    userDao.insertUser(user)
                    emit(Resource.Success(user))
                } else {
                    emit(Resource.Error("Invalid User Data"))
                }
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Login Failed: ${e.localizedMessage}"))
        }
    }

    override suspend fun getUser(): UserEntity? {
        return userDao.getUser()
    }

    override suspend fun logout() {
        userDao.clearUser()
    }
}
