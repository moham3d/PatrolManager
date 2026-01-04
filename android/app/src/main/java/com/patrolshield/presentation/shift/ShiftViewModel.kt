package com.patrolshield.presentation.shift

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.common.Resource
import com.patrolshield.common.UserPreferences
import com.patrolshield.domain.repository.PatrolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShiftViewModel @Inject constructor(
    private val patrolRepository: PatrolRepository,
    private val userPrefs: UserPreferences
) : ViewModel() {

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    val activeShift = patrolRepository.getActiveShift()

    private val _shiftEvent = MutableSharedFlow<ShiftEvent>()
    val shiftEvent = _shiftEvent.asSharedFlow()

    fun clockIn(lat: Double, lng: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val siteId = userPrefs.getSiteId()
            if (siteId == -1) {
                _shiftEvent.emit(ShiftEvent.Error("No assigned site found. Please re-login."))
                _isLoading.value = false
                return@launch
            }
            val result = patrolRepository.clockIn(lat, lng, siteId)
            _isLoading.value = false
            when (result) {
                is Resource.Success -> _shiftEvent.emit(ShiftEvent.ClockInSuccess)
                is Resource.Error -> _shiftEvent.emit(ShiftEvent.Error(result.message ?: "Clock-in failed"))
                else -> {}
            }
        }
    }

    fun clockOut(lat: Double, lng: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = patrolRepository.clockOut(lat, lng)
            _isLoading.value = false
            when (result) {
                is Resource.Success -> _shiftEvent.emit(ShiftEvent.ClockOutSuccess)
                is Resource.Error -> _shiftEvent.emit(ShiftEvent.Error(result.message ?: "Clock-out failed"))
                else -> {}
            }
        }
    }

    sealed class ShiftEvent {
        object ClockInSuccess : ShiftEvent()
        object ClockOutSuccess : ShiftEvent()
        data class Error(val message: String) : ShiftEvent()
    }
}
