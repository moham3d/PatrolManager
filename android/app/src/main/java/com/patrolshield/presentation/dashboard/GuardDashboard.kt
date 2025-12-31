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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.patrolshield.common.DateUtils


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardDashboard(
    viewModel: DashboardViewModel = hiltViewModel(),
    onStartPatrol: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val state = viewModel.state.value
    
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // User granted permission
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.loadSchedule(isRefresh = true)
        }
    }

    LaunchedEffect(state.isRefreshing) {
        if (state.isRefreshing) {
            pullToRefreshState.startRefresh()
        } else {
            pullToRefreshState.endRefresh()
        }
    }

    var showIncidentDialog by remember { mutableStateOf(false) }

    if (showIncidentDialog) {
        IncidentDialog(
            onDismiss = { showIncidentDialog = false },
            onSubmit = { type, priority, desc, lat, lng ->
                showIncidentDialog = false
                viewModel.reportIncident(type, priority, desc, lat, lng) {
                    android.widget.Toast.makeText(context, "Incident Reported", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    var showNotificationDialog by remember { mutableStateOf(false) }

    if (showNotificationDialog) {
        NotificationDialog(
            notifications = state.notifications,
            onDismiss = { showNotificationDialog = false },
            onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
            onClearAll = { viewModel.clearAllNotifications() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tungsten") },
                actions = {
                    // Notification Icon with Badge
                    IconButton(onClick = { showNotificationDialog = true }) {
                        Box {
                             Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onPrimary)
                            if (state.unreadCount > 0) {
                                Badge(
                                    modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                                ) {
                                    Text("${state.unreadCount}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
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
            FloatingActionButton(
                onClick = { showIncidentDialog = true },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Report Incident")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            
            // Active Patrol Banner
            if (state.activePatrol != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    onClick = onStartPatrol // Click to resume
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column {
                            val activeTemplate = state.schedules.find { it.id == state.activePatrol.templateId }
                            Text(activeTemplate?.name ?: "Patrol in Progress", style = MaterialTheme.typography.titleMedium)
                            Text("Started: ${DateUtils.formatTime(state.activePatrol.startTime)}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Button(onClick = onStartPatrol) {
                            Text("RESUME")
                        }
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
