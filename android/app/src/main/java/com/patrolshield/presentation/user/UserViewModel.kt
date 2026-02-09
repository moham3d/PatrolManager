package com.patrolshield.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.data.remote.dto.UserDto
import com.patrolshield.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserListState(
    val users: List<UserDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserListState())
    val state: StateFlow<UserListState> = _state.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.getUsers()
            if (result.isSuccess) {
                _state.value = _state.value.copy(users = result.getOrNull() ?: emptyList(), isLoading = false)
            } else {
                _state.value = _state.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }
}
