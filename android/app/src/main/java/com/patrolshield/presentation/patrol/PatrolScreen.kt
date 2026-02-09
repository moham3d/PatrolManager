package com.patrolshield.presentation.patrol

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrolshield.common.DateUtils
import com.patrolshield.common.LocationUtils
import com.patrolshield.presentation.dashboard.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatrolScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onEndPatrol: () -> Unit
) {
    val state = viewModel.state.value
    val context = androidx.compose.ui.platform.LocalContext.current
    var isScannerVisible by remember { mutableStateOf(false) }

    if (isScannerVisible && state.activePatrol?.remoteId != null) {
        CheckpointScannerScreen(
            runId = state.activePatrol.remoteId,
            onNavigateBack = { isScannerVisible = false }
        )
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Patrol") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { isScannerVisible = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                }

                ExtendedFloatingActionButton(
                    onClick = { 
                        val loc = LocationUtils.getLocation(context)
                        if (loc != null) {
                            viewModel.sendPanic(loc.latitude, loc.longitude)
                            android.widget.Toast.makeText(context, "SOS Sent with Location!", android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.sendPanic() // Send without location
                            android.widget.Toast.makeText(context, "SOS Sent (No Location Available)", android.widget.Toast.LENGTH_LONG).show()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "SOS")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SOS")
                }
            }
        },
        bottomBar = {
            BottomAppBar {
                Row(modifier = Modifier.padding(8.dp)) {
                    Button(
                        onClick = { isScannerVisible = true },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("SCAN TAG")
                    }
                    Button(
                        onClick = { 
                            viewModel.endPatrol(onSuccess = onEndPatrol)
                        },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("END PATROL")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Patrol Info
            state.activePatrol?.let { patrol ->
                Card(
                     modifier = Modifier.fillMaxWidth().padding(16.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Started at: ${DateUtils.formatTime(patrol.startTime)}", style = MaterialTheme.typography.bodyLarge)
                        Text("Status: In Progress", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            
                // Map
            Surface(modifier = Modifier.weight(1f), shadowElevation = 4.dp) {
                 androidx.compose.ui.viewinterop.AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        org.osmdroid.views.MapView(context).apply {
                            setMultiTouchControls(true)
                            controller.setZoom(18.0)
                            controller.setCenter(org.osmdroid.util.GeoPoint(30.0444, 31.2357))
                            
                            val locationOverlay = org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay(
                                org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider(context),
                                this
                            )
                            locationOverlay.enableMyLocation()
                            locationOverlay.runOnFirstFix {
                                val myLoc = locationOverlay.myLocation
                                if (myLoc != null) {
                                    (context as? android.app.Activity)?.runOnUiThread {
                                        controller.animateTo(myLoc)
                                    }
                                }
                            }
                            overlays.add(locationOverlay)
                            tag = locationOverlay // Save reference
                        }
                    },
                    update = { mapView ->
                        // Clear old markers (Keep LocationOverlay which is index 0 or tagged)
                        val locationOverlay = mapView.tag as? org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
                        mapView.overlays.clear()
                        if (locationOverlay != null) mapView.overlays.add(locationOverlay)

                        state.checkpoints.forEach { cp ->
                            val marker = org.osmdroid.views.overlay.Marker(mapView)
                            marker.position = org.osmdroid.util.GeoPoint(cp.lat, cp.lng)
                            marker.title = cp.name
                            marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                            mapView.overlays.add(marker)
                        }
                        mapView.invalidate()
                    }
                )
            }
        }
    }
}
