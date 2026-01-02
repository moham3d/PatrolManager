package com.patrolshield.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val token: String?,
    val user: UserDto?,
    val activeShift: ShiftDto? = null,
    val error: Boolean = false
)

data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val roleId: Int?,
    val Role: RoleDto? // Matches API structure
)

data class RoleDto(
    val name: String
)
