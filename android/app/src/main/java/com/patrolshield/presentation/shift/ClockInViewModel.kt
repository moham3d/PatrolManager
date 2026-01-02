package com.patrolshield.presentation.shift

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.SiteDto
import com.patrolshield.domain.repository.ShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClockInState(
    val sites: List<SiteDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class ClockInViewModel @Inject constructor(
    private val api: ApiService,
    private val shiftRepository: ShiftRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClockInState())
    val state: StateFlow<ClockInState> = _state.asStateFlow()

    init {
        loadSites()
    }

    fun loadSites() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = api.getSites()
                if (response.isSuccessful && response.body() != null) {
                    _state.value = _state.value.copy(
                        sites = response.body()!!.sites,
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to load sites: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun clockIn(siteId: Int, lat: Double, lng: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = shiftRepository.clockIn(siteId, lat, lng)
            if (result.isSuccess) {
                _state.value = _state.value.copy(isLoading = false, success = true)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Clock-in failed"
                )
            }
        }
    }
}
