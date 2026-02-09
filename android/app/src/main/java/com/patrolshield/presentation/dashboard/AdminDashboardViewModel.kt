package com.patrolshield.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.data.remote.dto.AdminStatsDto
import com.patrolshield.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardState(
    val stats: AdminStatsDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminDashboardState())
    val state: StateFlow<AdminDashboardState> = _state.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.getStats()
            if (result.isSuccess) {
                _state.value = _state.value.copy(stats = result.getOrNull(), isLoading = false)
            } else {
                _state.value = _state.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }
}
