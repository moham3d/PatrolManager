package com.patrolshield.presentation.patrol

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.common.Resource
import com.patrolshield.data.remote.dto.PatrolDto
import com.patrolshield.domain.repository.PatrolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatrolViewModel @Inject constructor(
    private val patrolRepository: PatrolRepository
) : ViewModel() {

    private val _schedule = mutableStateOf<List<PatrolDto>>(emptyList())
    val schedule: State<List<PatrolDto>> = _schedule

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _event = MutableSharedFlow<PatrolEvent>()
    val event = _event.asSharedFlow()

    init {
        loadSchedule()
    }

    fun loadSchedule() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = patrolRepository.getMySchedule()
            _isLoading.value = false
            if (result is Resource.Success) {
                _schedule.value = result.data ?: emptyList()
            } else {
                _event.emit(PatrolEvent.Error(result.message ?: "Failed to load schedule"))
            }
        }
    }

    fun startPatrol(patrolId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = patrolRepository.startPatrol(patrolId)
            _isLoading.value = false
            if (result is Resource.Success) {
                _event.emit(PatrolEvent.PatrolStarted(result.data!!, patrolId))
            } else {
                _event.emit(PatrolEvent.Error(result.message ?: "Failed to start patrol"))
            }
        }
    }

    sealed class PatrolEvent {
        data class PatrolStarted(val runId: Int, val patrolId: Int) : PatrolEvent()
        data class Error(val message: String) : PatrolEvent()
    }
}
