package com.patrolshield.presentation.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.common.Resource
import com.patrolshield.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    fun onEmailChange(value: String) {
        _email.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun login() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.login(_email.value, _password.value)
            _isLoading.value = false
            when (result) {
                is Resource.Success -> {
                    _loginEvent.emit(LoginEvent.Success(result.data?.user?.role ?: "guard"))
                }
                is Resource.Error -> {
                    _loginEvent.emit(LoginEvent.Error(result.message ?: "Unknown error"))
                }
                else -> {}
            }
        }
    }

    sealed class LoginEvent {
        data class Success(val role: String) : LoginEvent()
        data class Error(val message: String) : LoginEvent()
    }
}
