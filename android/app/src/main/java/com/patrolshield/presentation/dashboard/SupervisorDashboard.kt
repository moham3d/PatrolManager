package com.patrolshield.presentation.dashboard

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrolshield.common.DateUtils
import com.patrolshield.data.remote.dto.ActiveIncidentsDto
import com.patrolshield.data.remote.dto.LivePatrolDto
import com.patrolshield.data.remote.dto.PanicDto
import com.patrolshield.presentation.dashboard.IncidentResolutionDialog
import com.patrolshield.presentation.dashboard.SupervisorViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorDashboard(
    onLogout: () -> Unit,
    viewModel: SupervisorViewModel = hiltViewModel<SupervisorViewModel>()
) {
    val state = viewModel.state.value
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var incidentToResolve by remember { mutableStateOf<Int?>(null) }

    if (incidentToResolve != null) {
        IncidentResolutionDialog(
            incidentId = incidentToResolve!!,
            onDismiss = { incidentToResolve = null },
            onSubmit = { notes: String, evidenceUri: Uri? ->
                viewModel.resolveIncident(incidentToResolve!!, notes, evidenceUri) {
                    incidentToResolve = null
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Supervisor Command") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    titleContentColor = MaterialTheme.colorScheme.onTertiary,
                    actionIconContentColor = MaterialTheme.colorScheme.onTertiary
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                    Text("Overview", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                    BadgedBox(badge = {
                        if (state.incidents.isNotEmpty() || state.panics.isNotEmpty()) {
                            Badge { Text("${state.incidents.size + state.panics.size}") }
                        }
                    }) {
                        Text("Incidents", modifier = Modifier.padding(16.dp))
                    }
                }
            }

            if (selectedTabIndex == 0) {
                // Map View (Top Half)
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                   AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            MapView(context).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                controller.setZoom(15.0)
                                controller.setCenter(GeoPoint(30.0444, 31.2357)) 
                                setMultiTouchControls(true)
                            }
                        },
                        update = { mapView ->
                            mapView.overlays.clear()

                            (state.livePatrols as Iterable<LivePatrolDto>).forEach { patrol: LivePatrolDto ->
                                val marker = Marker(mapView)
                                marker.position = GeoPoint(patrol.lat, patrol.lng)
                                marker.title = "${patrol.guardName} (${patrol.status})"
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                mapView.overlays.add(marker)
                            }

                            mapView.invalidate()
                        }
                    ) 
                    
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                // Active Patrols List (Bottom Half)
                Surface(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column {
                        Text(
                            "Active Guards: ${state.livePatrols.size}", 
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                        
                        if (state.livePatrols.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No guards active.")
                            }
                        } else {
                            LazyColumn {
                                items(state.livePatrols) { guard ->
                                    ListItem(
                                        headlineContent = { Text(guard.guardName) },
                                        supportingContent = { Text("Last seen: ${DateUtils.formatTime(guard.lastSeen)}") },
                                        leadingContent = {
                                            val color = when (guard.status) {
                                                "active" -> Color.Green
                                                "SOS" -> Color.Red
                                                else -> Color.Gray
                                            }
                                            Badge(containerColor = color)
                                        },
                                        trailingContent = {
                                            Text(guard.status.uppercase(), style = MaterialTheme.typography.labelSmall)
                                        }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            } else {
                // Incidents List
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Panic Alerts First
                    if (state.panics.isNotEmpty()) {
                        item {
                            Surface(color = MaterialTheme.colorScheme.errorContainer) {
                                Text("Panic Alerts", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth().padding(8.dp))
                            }
                        }
                        items(state.panics) { panic ->
                            ListItem(
                                headlineContent = { Text("SOS: Guard #${panic.guardId}", color = MaterialTheme.colorScheme.error) },
                                supportingContent = { Text("Triggered at ${panic.triggeredAt}") },
                                leadingContent = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                            HorizontalDivider()
                        }
                    }

                    // Active Incidents
                    if (state.incidents.isNotEmpty()) {
                        item {
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text("Active Incidents", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth().padding(8.dp))
                            }
                        }
                        items(state.incidents) { inc ->
                            ListItem(
                                headlineContent = { Text("${inc.type} (ID: ${inc.id})") },
                                supportingContent = { Text(inc.description ?: "No description") },
                                trailingContent = {
                                    Button(onClick = { incidentToResolve = inc.id }) {
                                        Text("RESOLVE")
                                    }
                                }
                            )
                            HorizontalDivider()
                        }
                    }

                    if (state.incidents.isEmpty() && state.panics.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No active incidents.")
                            }
                        }
                    }
                }
            }
        }
    }
}
