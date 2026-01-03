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

import com.patrolshield.data.remote.dto.IncidentDto
import com.patrolshield.data.remote.dto.PanicDto

data class SupervisorState(
    val livePatrols: List<LivePatrolDto> = emptyList(),
    val incidents: List<IncidentDto> = emptyList(),
    val panics: List<PanicDto> = emptyList(),
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
                fetchActiveIncidents()
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
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(error = result.message)
                }
                is Resource.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun fetchActiveIncidents() {
        supervisorRepository.getActiveIncidents().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        incidents = result.data?.incidents ?: emptyList(),
                        panics = result.data?.panics ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(error = result.message, isLoading = false)
                }
                is Resource.Loading -> {
                    if (_state.value.incidents.isEmpty()) {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun resolveIncident(id: Int, notes: String, evidenceUri: android.net.Uri?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            supervisorRepository.resolveIncident(id, notes, evidenceUri).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        fetchActiveIncidents()
                        onSuccess()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(error = result.message)
                    }
                    is Resource.Loading -> {}
                }
            }.launchIn(this)
        }
    }
}
