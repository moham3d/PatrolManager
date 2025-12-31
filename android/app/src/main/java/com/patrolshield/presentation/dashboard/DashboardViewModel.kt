package com.patrolshield.presentation.dashboard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.common.Resource
import com.patrolshield.data.local.entities.PatrolEntity
import com.patrolshield.data.local.entities.CheckpointEntity
import com.patrolshield.data.local.entities.NotificationEntity
import com.patrolshield.data.remote.dto.PatrolDto
import com.patrolshield.domain.repository.PatrolRepository
import com.patrolshield.domain.repository.IncidentRepository
import com.patrolshield.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false, // Added for PullToRefresh
    val schedules: List<PatrolDto> = emptyList(),
    val activePatrol: PatrolEntity? = null,
    val completedPatrols: List<PatrolEntity> = emptyList(),
    val checkpoints: List<CheckpointEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: PatrolRepository,
    private val incidentRepository: IncidentRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = mutableStateOf(DashboardState())
    val state: State<DashboardState> = _state

    init {
        loadSchedule()
        observeActivePatrol()
        observeNotifications()
        
        // Simulating a notification for demo
        viewModelScope.launch { 
            notificationRepository.createSampleNotification() 
        }
    }

    fun loadSchedule(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _state.value = _state.value.copy(isRefreshing = true)
            } else {
                _state.value = _state.value.copy(isLoading = true)
            }
            
            repository.getMySchedule().collect { result ->
                val completed = repository.getCompletedPatrols()
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            schedules = result.data ?: emptyList(),
                            isLoading = false,
                            isRefreshing = false,
                            completedPatrols = completed
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                    is Resource.Loading -> {
                        // handled above
                    }
                }
            }
        }
    }

    private fun observeActivePatrol() {
        repository.getActivePatrol().onEach { patrol ->
            _state.value = _state.value.copy(activePatrol = patrol)
            if (patrol != null) {
                loadCheckpoints(patrol.templateId)
            } else {
                _state.value = _state.value.copy(checkpoints = emptyList())
            }
        }.launchIn(viewModelScope)
    }
    
    private fun observeNotifications() {
        notificationRepository.getNotifications().onEach { list ->
            _state.value = _state.value.copy(notifications = list)
        }.launchIn(viewModelScope)

        notificationRepository.getUnreadCount().onEach { count ->
             _state.value = _state.value.copy(unreadCount = count)
        }.launchIn(viewModelScope)
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            notificationRepository.clearAll()
        }
    }
    
    private fun loadCheckpoints(templateId: Int) {
        viewModelScope.launch {
            val checkpoints = repository.getCheckpoints(templateId)
            _state.value = _state.value.copy(checkpoints = checkpoints)
        }
    }

    fun startPatrol(templateId: Int) {
        viewModelScope.launch {
            repository.startPatrol(templateId).onEach { result ->
                if (result is Resource.Error) {
                    _state.value = _state.value.copy(error = result.message)
                }
            }.launchIn(this)
        }
    }

    fun sendPanic(lat: Double? = null, lng: Double? = null) {
        viewModelScope.launch {
            val runId = _state.value.activePatrol?.remoteId
            repository.sendPanic(lat, lng, runId).let { result ->
                if (result is Resource.Error) {
                   // Toast or snackbar handled in UI via side effect? 
                   // Ideally use Channel, but for now we logic in UI catches error via State? No, Repo return resource.
                   // We just fire and forget for SOS usually, best effort.
                }
            }
        }
    }

    fun scanCheckpoint(runId: Int, checkpointId: Int, lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.scanCheckpoint(runId, checkpointId, lat, lng).collect { result ->
                when(result) {
                    is Resource.Success -> {
                        // Mark locally as scanned? 
                        // For now just rely on Toast/UI feedback
                    }
                    is Resource.Error -> {
                         // Handle error
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun endPatrol(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.endPatrol().onEach { result ->
                if (result is Resource.Error) {
                    _state.value = _state.value.copy(error = result.message)
                } else if (result is Resource.Success) {
                    _state.value = _state.value.copy(activePatrol = null)
                    onSuccess()
                }
            }.launchIn(this)
        }
    }

    fun reportIncident(
        type: String,
        priority: String,
        description: String,
        lat: Double?,
        lng: Double?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // Determine Site ID
            val activeSiteId = _state.value.activePatrol?.siteId
            val scheduleSiteId = _state.value.schedules.firstOrNull()?.siteId
            
            // Fallback to 1 if nothing else found (should be rare if schedules exist)
            val siteId = activeSiteId ?: scheduleSiteId ?: 1 
            
            incidentRepository.reportIncident(type, priority, description, siteId, lat, lng).collect { result ->
                when(result) {
                    is Resource.Success -> {
                        val message = if (result.data == null) "Incident saved locally" else "Incident reported successfully"
                        onSuccess() // Let UI handle toast
                    }
                    is Resource.Error -> {
                         // Even error might mean saved locally if we handled it that way in repo, 
                         // but here we just pass up. 
                         _state.value = _state.value.copy(error = result.message)
                    }
                    is Resource.Loading -> {
                        // Optional: show loading in dialog
                    }
                }
            }
        }
    }
}
