package com.patrolshield.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.common.Resource
import com.patrolshield.data.remote.dto.ManagerStatsDto
import com.patrolshield.domain.repository.ManagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagerViewModel @Inject constructor(
    private val repository: ManagerRepository
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<ManagerStatsDto>>(Resource.Loading())
    val state: StateFlow<Resource<ManagerStatsDto>> = _state

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            repository.getStats().collect {
                _state.value = it
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboard(
    onLogout: () -> Unit,
    viewModel: ManagerViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Site Manager") },
                actions = {
                    IconButton(onClick = { viewModel.loadStats() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (state) {
                is Resource.Loading -> CircularProgressIndicator(Modifier.fillMaxSize().wrapContentSize())
                is Resource.Error -> Text(
                    text = state.message ?: "Error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxSize().wrapContentSize()
                )
                is Resource.Success -> {
                    val stats = state.data!!
                    Column(Modifier.padding(16.dp)) {
                        // Stats Row
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatCard("Patrols", stats.patrolsToday.toString(), Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            StatCard("Incidents", stats.incidentsToday.toString(), Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(16.dp))
                        
                        Text("Recent Incidents", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        
                        LazyColumn {
                            items(stats.recentIncidents) { incident ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(
                                            text = "${incident.type} - ${incident.status.uppercase()}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(text = incident.description ?: "", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = "Reported by: ${incident.reporter.name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineLarge)
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
