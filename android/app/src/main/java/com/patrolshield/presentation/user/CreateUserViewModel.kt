package com.patrolshield.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateUserState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class CreateUserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateUserState())
    val state: StateFlow<CreateUserState> = _state.asStateFlow()

    fun createUser(name: String, email: String, password: String, roleId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.createUser(name, email, password, roleId)
            if (result.isSuccess) {
                _state.value = _state.value.copy(isLoading = false, success = true)
            } else {
                _state.value = _state.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }
    
    fun resetState() {
        _state.value = CreateUserState()
    }
}
