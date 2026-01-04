package com.patrolshield.presentation.incident

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.common.Resource
import com.patrolshield.data.remote.dto.IncidentRequest
import com.patrolshield.domain.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class IncidentViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _event = MutableSharedFlow<IncidentEvent>()
    val event = _event.asSharedFlow()

    fun reportIncident(
        type: String,
        priority: String,
        description: String,
        siteId: Int,
        lat: Double?,
        lng: Double?,
        patrolRunId: Int? = null,
        imageFile: File? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val request = IncidentRequest(type, priority, description, siteId, lat, lng, patrolRunId)
            val result = incidentRepository.reportIncident(request, imageFile)
            _isLoading.value = false
            
            if (result is Resource.Success) {
                _event.emit(IncidentEvent.Success)
            } else {
                _event.emit(IncidentEvent.Error(result.message ?: "Failed to report incident"))
            }
        }
    }

    sealed class IncidentEvent {
        object Success : IncidentEvent()
        data class Error(val message: String) : IncidentEvent()
    }
}
