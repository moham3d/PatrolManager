package com.patrolshield.presentation.dashboard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.common.Resource
import com.patrolshield.data.remote.dto.LivePatrolDto
import com.patrolshield.domain.repository.SupervisorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupervisorState(
    val livePatrols: List<LivePatrolDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SupervisorViewModel @Inject constructor(
    private val supervisorRepository: SupervisorRepository
) : ViewModel() {

    private val _state = mutableStateOf(SupervisorState())
    val state: State<SupervisorState> = _state

    init {
        startLiveTracking()
    }

    private fun startLiveTracking() {
        viewModelScope.launch {
            while(true) {
                fetchLivePatrols()
                delay(30_000L) // Poll every 30 seconds
            }
        }
    }

    private suspend fun fetchLivePatrols() {
        supervisorRepository.getLivePatrols().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        livePatrols = result.data ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                is Resource.Loading -> {
                     // Don't show loading on poll updates to avoid flickering
                     if (_state.value.livePatrols.isEmpty()) {
                         _state.value = _state.value.copy(isLoading = true)
                     }
                }
            }
        }.launchIn(viewModelScope)
    }
}
