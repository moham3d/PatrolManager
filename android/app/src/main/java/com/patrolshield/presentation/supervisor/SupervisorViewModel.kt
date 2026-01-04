package com.patrolshield.presentation.supervisor

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.common.Resource
import com.patrolshield.data.remote.dto.Incident
import com.patrolshield.domain.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SupervisorViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _incidents = mutableStateOf<List<Incident>>(emptyList())
    val incidents: State<List<Incident>> = _incidents

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _event = MutableSharedFlow<SupervisorEvent>()
    val event = _event.asSharedFlow()

    init {
        loadIncidents()
    }

    fun loadIncidents() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = incidentRepository.getActiveIncidents()
            _isLoading.value = false
            if (result is Resource.Success) {
                _incidents.value = result.data ?: emptyList()
            } else {
                _event.emit(SupervisorEvent.Error(result.message ?: "Failed to load incidents"))
            }
        }
    }

    fun resolveIncident(incidentId: Int, comment: String, imageFile: File?) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = incidentRepository.resolveIncident(incidentId, comment, imageFile)
            _isLoading.value = false
            if (result is Resource.Success) {
                _event.emit(SupervisorEvent.IncidentResolved)
                loadIncidents() // Refresh list
            } else {
                _event.emit(SupervisorEvent.Error(result.message ?: "Failed to resolve incident"))
            }
        }
    }

    sealed class SupervisorEvent {
        object IncidentResolved : SupervisorEvent()
        data class Error(val message: String) : SupervisorEvent()
    }
}
