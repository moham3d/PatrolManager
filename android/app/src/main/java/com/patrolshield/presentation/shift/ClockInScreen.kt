package com.patrolshield.presentation.shift

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.patrolshield.data.remote.dto.SiteDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockInScreen(
    onClockInSuccess: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ClockInViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var location by remember { mutableStateOf<android.location.Location?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var selectedSite by remember { mutableStateOf<SiteDto?>(null) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                @SuppressLint("MissingPermission")
                val task = fusedLocationClient.lastLocation
                task.addOnSuccessListener { loc ->
                     if (loc != null) location = loc
                     else locationError = "GPS signal lost. Move outside."
                }
            } catch (e: Exception) {
                locationError = "Error getting location"
            }
        } else {
            locationError = "Location permission denied"
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            onClockInSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Start Shift") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Site to Clock In", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))

                if (location != null) {
                    Text("GPS Locked: ${location!!.latitude}, ${location!!.longitude}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                } else if (locationError != null) {
                    Text(locationError!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                        Text("Retry GPS")
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Acquiring GPS...", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (state.isLoading) {
                    CircularProgressIndicator()
                } else if (state.error != null) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.loadSites() }) { Text("Retry") }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(state.sites) { site ->
                            SiteCard(
                                site = site,
                                isSelected = site.id == selectedSite?.id,
                                onClick = { selectedSite = site }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (selectedSite != null && location != null) {
                            viewModel.clockIn(selectedSite!!.id, location!!.latitude, location!!.longitude)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = selectedSite != null && location != null && !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                         Text("CLOCK IN NOW")
                    }
                }
            }
        }
    }
}

@Composable
fun SiteCard(
    site: SiteDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = if (isSelected) 
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else 
            CardDefaults.cardColors(),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(site.name, style = MaterialTheme.typography.titleMedium)
            site.address?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
