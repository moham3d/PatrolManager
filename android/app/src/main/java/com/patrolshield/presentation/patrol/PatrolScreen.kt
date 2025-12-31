package com.patrolshield.presentation.patrol

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrolshield.common.DateUtils
import com.patrolshield.presentation.dashboard.DashboardViewModel

fun getLocation(context: android.content.Context): android.location.Location? {
    // Note: This is a synchronous check for last known location. 
    // Ideally we should use a proper Location callback flow, but for this POC it works.
    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
    return try {
        locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
    } catch (e: SecurityException) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatrolScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onEndPatrol: () -> Unit
) {
    val state = viewModel.state.value
    val context = androidx.compose.ui.platform.LocalContext.current
    
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
             ExtendedFloatingActionButton(
                onClick = { 
                    val loc = getLocation(context)
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
        },
        bottomBar = {
            BottomAppBar {
                Row(modifier = Modifier.padding(8.dp)) {
                    Button(
                        onClick = {
                           try {
                               val loc = getLocation(context)

                               if (loc != null && state.activePatrol?.remoteId != null) {
                                   val nearest = state.checkpoints.minByOrNull { 
                                       val res = FloatArray(1)
                                       android.location.Location.distanceBetween(loc.latitude, loc.longitude, it.lat, it.lng, res)
                                       res[0]
                                   }

                                   if (nearest != null) {
                                       val res = FloatArray(1)
                                       android.location.Location.distanceBetween(loc.latitude, loc.longitude, nearest.lat, nearest.lng, res)
                                       val dist = res[0]
                                       
                                       // 100 meters threshold
                                       if (dist <= 100) {
                                            viewModel.scanCheckpoint(state.activePatrol.remoteId, nearest.id, loc.latitude, loc.longitude)
                                            android.widget.Toast.makeText(context, "Scanned: " + nearest.name, android.widget.Toast.LENGTH_SHORT).show()
                                       } else {
                                           android.widget.Toast.makeText(context, "Too far from " + nearest.name + " (" + dist.toInt() + "m)", android.widget.Toast.LENGTH_SHORT).show()
                                       }
                                   }
                               } else {
                                   android.widget.Toast.makeText(context, "Searching for GPS...", android.widget.Toast.LENGTH_SHORT).show()
                               }
                           } catch (e: Exception) {
                               android.widget.Toast.makeText(context, "Error: " + e.message, android.widget.Toast.LENGTH_SHORT).show()
                           }
                        },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("CHECK IN")
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
                            // TODO: Change icon based on cp.isScanned (if we had it)
                            mapView.overlays.add(marker)
                        }
                        mapView.invalidate()
                    }
                )
            }
        }
    }
}
