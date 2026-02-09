package com.patrolshield.data.remote.dto

data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val roleId: Int
)

data class CreateUserResponse(
    val message: String,
    val user: UserDto
)
