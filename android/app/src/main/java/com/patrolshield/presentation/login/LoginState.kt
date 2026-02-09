package com.patrolshield.presentation.login

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val role: String? = null,
    val error: String? = null
)

sealed class LoginEvent {
    data class EnteredEmail(val value: String): LoginEvent()
    data class EnteredPassword(val value: String): LoginEvent()
    object Login: LoginEvent()
}
