package com.patrolshield.presentation.visitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.data.remote.dto.VisitorDto
import com.patrolshield.domain.repository.VisitorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VisitorState(
    val visitors: List<VisitorDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VisitorViewModel @Inject constructor(
    private val repository: VisitorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VisitorState())
    val state: StateFlow<VisitorState> = _state.asStateFlow()

    init {
        loadVisitors()
    }

    fun loadVisitors() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.getVisitorsToday()
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    visitors = result.getOrDefault(emptyList()),
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }

    fun checkInVisitor(visitorId: Int) {
        viewModelScope.launch {
            // Optimistic update or just reload? Let's show loading
            _state.value = _state.value.copy(isLoading = true)
            val result = repository.checkInVisitor(visitorId)
            
            if (result.isSuccess) {
                // Refresh list
                loadVisitors()
            } else {
                 _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Check-in failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }
}
