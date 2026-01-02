package com.patrolshield.presentation.site

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.domain.repository.SiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateSiteState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class CreateSiteViewModel @Inject constructor(
    private val repository: SiteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateSiteState())
    val state: StateFlow<CreateSiteState> = _state.asStateFlow()

    fun createSite(name: String, address: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.createSite(name, address, lat, lng)
            if (result.isSuccess) {
                _state.value = _state.value.copy(isLoading = false, success = true)
            } else {
                _state.value = _state.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun resetState() {
        _state.value = CreateSiteState()
    }
}
