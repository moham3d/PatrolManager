package com.patrolshield.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrolshield.common.DateUtils
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorDashboard(
    viewModel: SupervisorViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val state = viewModel.state.value

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
            
            // Map View (Top Half)
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
               AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        MapView(context).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            controller.setZoom(15.0)
                            // Default center (Cairo for demo)
                            controller.setCenter(GeoPoint(30.0444, 31.2357)) 
                            setMultiTouchControls(true)
                        }
                    },
                    update = { mapView ->
                        // Clear existing markers to avoid dupes (naive implementation)
                        mapView.overlays.clear()
                        
                        state.livePatrols.forEach { patrol ->
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
                                        // Status Indicator
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
        }
    }
}
