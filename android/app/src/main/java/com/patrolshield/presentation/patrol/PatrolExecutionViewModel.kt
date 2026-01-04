package com.patrolshield.presentation.patrol

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.common.NfcManager
import com.patrolshield.common.Resource
import com.patrolshield.data.remote.dto.CheckpointDto
import com.patrolshield.domain.repository.PatrolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatrolExecutionViewModel @Inject constructor(
    private val patrolRepository: PatrolRepository
) : ViewModel() {

    private val _checkpoints = mutableStateOf<List<CheckpointDto>>(emptyList())
    val checkpoints: State<List<CheckpointDto>> = _checkpoints

    private val _currentIndex = mutableStateOf(0)
    val currentIndex: State<Int> = _currentIndex

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _event = MutableSharedFlow<ExecutionEvent>()
    val event = _event.asSharedFlow()

    private var runId: Int = -1

    init {
        viewModelScope.launch {
            NfcManager.nfcTag.collectLatest { tagId ->
                handleScan("NFC", tagId)
            }
        }
    }

    fun initPatrol(patrolId: Int, runId: Int) {
        this.runId = runId
        viewModelScope.launch {
            val result = patrolRepository.getMySchedule()
            if (result is Resource.Success) {
                val patrol = result.data?.find { it.id == patrolId }
                _checkpoints.value = patrol?.checkpoints?.sortedBy { it.order } ?: emptyList()
            }
        }
    }

    fun handleScan(type: String, value: String, lat: Double? = null, lng: Double? = null) {
        val currentCheckpoint = _checkpoints.value.getOrNull(_currentIndex.value) ?: return
        
        if (currentCheckpoint.type != type) {
            viewModelScope.launch { _event.emit(ExecutionEvent.Error("Wrong scanner type! Use ${currentCheckpoint.type}")) }
            return
        }

        if (currentCheckpoint.value != value) {
            viewModelScope.launch { _event.emit(ExecutionEvent.Error("Wrong checkpoint scanned!")) }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = patrolRepository.scanCheckpoint(runId, currentCheckpoint.id, value, lat, lng)
            _isLoading.value = false
            
            if (result is Resource.Success) {
                _currentIndex.value++
                if (_currentIndex.value >= _checkpoints.value.size) {
                    _event.emit(ExecutionEvent.PatrolFinished)
                } else {
                    _event.emit(ExecutionEvent.CheckpointSuccess)
                }
            } else {
                _event.emit(ExecutionEvent.Error(result.message ?: "Scan failed"))
            }
        }
    }

    fun endPatrol() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = patrolRepository.endPatrol(runId)
            _isLoading.value = false
            if (result is Resource.Success) {
                _event.emit(ExecutionEvent.PatrolFinished)
            } else {
                _event.emit(ExecutionEvent.Error(result.message ?: "Failed to end patrol"))
            }
        }
    }

    fun triggerPanic(lat: Double, lng: Double) {
        viewModelScope.launch {
            val result = patrolRepository.triggerPanic(lat, lng, runId)
            if (result is Resource.Success) {
                _event.emit(ExecutionEvent.Error("SOS Alert Sent!"))
            } else {
                _event.emit(ExecutionEvent.Error("Failed to send SOS: ${result.message}"))
            }
        }
    }

    sealed class ExecutionEvent {
        object CheckpointSuccess : ExecutionEvent()
        object PatrolFinished : ExecutionEvent()
        data class Error(val message: String) : ExecutionEvent()
    }
}
