package com.patrolshield.presentation.patrol

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatrolListScreen(
    onPatrolStarted: (Int, Int) -> Unit,
    viewModel: PatrolViewModel = hiltViewModel()
) {
    val schedule = viewModel.schedule.value
    val isLoading = viewModel.isLoading.value
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is PatrolViewModel.PatrolEvent.PatrolStarted -> {
                    onPatrolStarted(event.runId, event.patrolId)
                }
                is PatrolViewModel.PatrolEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("My Patrol Schedule") })
        }
    ) { padding ->
        if (isLoading && schedule.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(schedule) { patrol ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.startPatrol(patrol.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = patrol.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${patrol.checkpoints.size} Checkpoints",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                        }
                    }
                }
            }
        }
    }
}
