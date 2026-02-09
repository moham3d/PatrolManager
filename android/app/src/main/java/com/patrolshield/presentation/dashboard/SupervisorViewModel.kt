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

import com.patrolshield.data.remote.dto.ActiveIncidentDto
import com.patrolshield.data.remote.dto.ActiveIncidentsDto
import com.patrolshield.data.remote.dto.PanicDto

data class SupervisorState(
    val livePatrols: List<LivePatrolDto> = emptyList(),
    val incidents: List<ActiveIncidentDto> = emptyList(),
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
        supervisorRepository.getLivePatrols().onEach { result: Resource<List<LivePatrolDto>> ->
            when (result) {
                is Resource.Success<List<LivePatrolDto>> -> {
                    _state.value = _state.value.copy(
                        livePatrols = result.data ?: emptyList(),
                        error = null
                    )
                }
                is Resource.Error<List<LivePatrolDto>> -> {
                    _state.value = _state.value.copy(error = result.message)
                }
                is Resource.Loading<List<LivePatrolDto>> -> {}
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun fetchActiveIncidents() {
        supervisorRepository.getActiveIncidents().onEach { result: Resource<ActiveIncidentsDto> ->
            when (result) {
                is Resource.Success<ActiveIncidentsDto> -> {
                    _state.value = _state.value.copy(
                        incidents = result.data?.incidents ?: emptyList(),
                        panics = result.data?.panics ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error<ActiveIncidentsDto> -> {
                    _state.value = _state.value.copy(error = result.message, isLoading = false)
                }
                is Resource.Loading<ActiveIncidentsDto> -> {
                    if (_state.value.incidents.isEmpty()) {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun resolveIncident(id: Int, notes: String, evidenceUri: android.net.Uri?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            supervisorRepository.resolveIncident(id, notes, evidenceUri).onEach { result: Resource<Unit> ->
                when (result) {
                    is Resource.Success<Unit> -> {
                        fetchActiveIncidents()
                        onSuccess()
                    }
                    is Resource.Error<Unit> -> {
                        _state.value = _state.value.copy(error = result.message)
                    }
                    is Resource.Loading<Unit> -> {}
                }
            }.launchIn(this)
        }
    }
}
