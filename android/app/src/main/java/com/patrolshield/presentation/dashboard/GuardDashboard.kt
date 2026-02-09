package com.patrolshield.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.patrolshield.common.DateUtils


import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

@Composable
fun ShiftTimer(startTime: Long) {
    var elapsedSeconds by remember { mutableStateOf((System.currentTimeMillis() - startTime) / 1000) }

    LaunchedEffect(startTime) {
        while (true) {
            delay(1000)
            elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
        }
    }

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60

    Text(
        text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardDashboard(
    viewModel: DashboardViewModel = hiltViewModel(),
    onStartPatrol: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToVisitors: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state
    val pullToRefreshState = rememberPullToRefreshState()
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadSchedule()
        }
    }
    
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            viewModel.loadSchedule(isRefresh = true)
            pullToRefreshState.endRefresh()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PatrolShield") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (state.unreadCount > 0) {
                                Badge { Text(state.unreadCount.toString()) }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(onClick = { /* Open Notifications */ }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    val loc = com.patrolshield.common.LocationUtils.getLocation(context)
                    viewModel.sendPanic(loc?.latitude, loc?.longitude)
                    android.widget.Toast.makeText(context, "SOS SENT!", android.widget.Toast.LENGTH_LONG).show()
                },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("PANIC")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            
            // Active Shift Card with Timer
            // Note: Since we don't have activeShift.startTime in state yet, 
            // we'll assume a dummy or update DashboardState soon.
            // For now, let's use the activePatrol.startTime as a proxy or just show the UI.
            
            state.activePatrol?.let { activePatrol ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text("SHIFT DURATION", style = MaterialTheme.typography.labelLarge)
                        ShiftTimer(startTime = activePatrol.startTime)
                        Spacer(modifier = Modifier.height(8.dp))
                        val patrol = state.schedules.find { it.id == activePatrol.templateId }
                        Text(
                            "Location: Site ${patrol?.siteId ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ... Visitor Management ...
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                onClick = onNavigateToVisitors,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Visitor Management", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("Check in/out guests", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            // Schedule Section
            Text(
                "My Schedule", 
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
            
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.schedules.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("No patrols scheduled today.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadSchedule() }) {
                            Text("Refresh")
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().nestedScroll(pullToRefreshState.nestedScrollConnection)) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.schedules) { patrol ->
                            val isRunActive = state.activePatrol?.templateId == patrol.id
                            val isCompleted = state.completedPatrols.any { it.templateId == patrol.id }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = when {
                                    isRunActive -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                                    isCompleted -> CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.5f))
                                    else -> CardDefaults.cardColors()
                                },
                                onClick = { 
                                    if (!isCompleted && state.activePatrol == null) {
                                        if (androidx.core.content.ContextCompat.checkSelfPermission(
                                            context, 
                                            android.Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                            viewModel.startPatrol(patrol.id)
                                            onStartPatrol()
                                        } else {
                                            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                        }
                                    } else if (isRunActive) {
                                        onStartPatrol()
                                    }
                                }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        if (isRunActive) {
                                            Icon(Icons.Default.Menu, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(
                                            patrol.name, 
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isCompleted) Color.Gray else Color.Unspecified
                                        )
                                        if (isCompleted) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text("DONE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                    }
                                    Text(patrol.description ?: "No description", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Button(
                                        onClick = { 
                                            if (isRunActive) {
                                                 onStartPatrol()
                                            } else {
                                                if (androidx.core.content.ContextCompat.checkSelfPermission(
                                                    context, 
                                                    android.Manifest.permission.ACCESS_FINE_LOCATION
                                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                                    viewModel.startPatrol(patrol.id)
                                                    onStartPatrol()
                                                } else {
                                                    locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                                }
                                            }
                                        },
                                        enabled = !isCompleted && (state.activePatrol == null || isRunActive),
                                        colors = if (isCompleted) ButtonDefaults.buttonColors(containerColor = Color.Gray) else ButtonDefaults.buttonColors()
                                    ) {
                                        Text(
                                            when {
                                                isRunActive -> "IN PROGRESS"
                                                isCompleted -> "COMPLETED"
                                                else -> "START PATROL"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    if (pullToRefreshState.isRefreshing || pullToRefreshState.progress > 0f) {
                        PullToRefreshContainer(
                            state = pullToRefreshState,
                            modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }
}
