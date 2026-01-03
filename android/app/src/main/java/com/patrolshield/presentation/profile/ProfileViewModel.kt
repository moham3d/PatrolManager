package com.patrolshield.presentation.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.data.local.entities.UserEntity
import com.patrolshield.domain.repository.AuthRepository
import com.patrolshield.domain.repository.PatrolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: UserEntity? = null,
    val completedPatrolsCount: Int = 0,
    val distanceWalkedKm: Double = 0.0,
    val incidentCount: Int = 0,
    val isDarkMode: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val patrolRepository: PatrolRepository,
    private val incidentRepository: com.patrolshield.domain.repository.IncidentRepository
) : ViewModel() {

    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val user = authRepository.getUser()
            val patrols = patrolRepository.getCompletedPatrols()
            
            // Mocking some stats for demo
            val distance = patrols.size * 1.5 // Assume 1.5km per patrol
            
            _state.value = _state.value.copy(
                user = user,
                completedPatrolsCount = patrols.size,
                distanceWalkedKm = distance,
                incidentCount = 2 // Mocked
            )
        }
    }

    fun toggleDarkMode() {
        _state.value = _state.value.copy(isDarkMode = !_state.value.isDarkMode)
        // Ideally this would save to DataStore and update AppTheme
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogout()
        }
    }
}
