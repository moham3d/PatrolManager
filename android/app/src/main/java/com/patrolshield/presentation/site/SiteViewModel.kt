package com.patrolshield.presentation.site

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.data.remote.dto.SiteDto
import com.patrolshield.domain.repository.SiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SiteListState(
    val sites: List<SiteDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SiteViewModel @Inject constructor(
    private val repository: SiteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SiteListState())
    val state: StateFlow<SiteListState> = _state.asStateFlow()

    init {
        loadSites()
    }

    fun loadSites() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.getSites()
            if (result.isSuccess) {
                _state.value = _state.value.copy(sites = result.getOrNull() ?: emptyList(), isLoading = false)
            } else {
                _state.value = _state.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }
}
