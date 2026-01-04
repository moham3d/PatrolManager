package com.patrolshield.presentation.supervisor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrolshield.common.ImageUtils
import com.patrolshield.data.remote.dto.Incident
import kotlinx.coroutines.flow.collectLatest
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorDashboard(
    viewModel: SupervisorViewModel = hiltViewModel()
) {
    val incidents = viewModel.incidents.value
    val isLoading = viewModel.isLoading.value
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Dialog state
    var showResolveDialog by remember { mutableStateOf(false) }
    var selectedIncidentId by remember { mutableStateOf<Int?>(null) }
    var resolutionComment by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current

    // Camera & Gallery Launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = tempCameraUri
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is SupervisorViewModel.SupervisorEvent.IncidentResolved -> {
                    snackbarHostState.showSnackbar("Incident Resolved Successfully")
                    showResolveDialog = false
                    resolutionComment = ""
                    selectedImageUri = null
                }
                is SupervisorViewModel.SupervisorEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Supervisor Dashboard") }) }
    ) { padding ->
        if (isLoading && incidents.isEmpty()) {
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
                item {
                    Text("Active Incidents", style = MaterialTheme.typography.titleLarge)
                }
                
                if (incidents.isEmpty()) {
                    item {
                        Text("No active incidents", modifier = Modifier.padding(vertical = 16.dp))
                    }
                }

                items(incidents) { incident ->
                    IncidentCard(
                        incident = incident,
                        onResolveClick = {
                            selectedIncidentId = incident.id
                            showResolveDialog = true
                        }
                    )
                }
            }
        }

        if (showResolveDialog) {
            AlertDialog(
                onDismissRequest = { showResolveDialog = false },
                title = { Text("Resolve Incident #${selectedIncidentId}") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = resolutionComment,
                            onValueChange = { resolutionComment = it },
                            label = { Text("Resolution Comment") },
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                val photoFile = ImageUtils.createTempImageFile(context)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            }) {
                                Text("Camera")
                            }
                            
                            Button(onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }) {
                                Text("Gallery")
                            }
                        }
                        
                        if (selectedImageUri != null) {
                            Text("Image Attached", color = Color.Green, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val imageFile = selectedImageUri?.let { ImageUtils.compressImage(it, context) }
                        selectedIncidentId?.let { id ->
                            viewModel.resolveIncident(id, resolutionComment, imageFile)
                        }
                    }) {
                        Text("Resolve")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResolveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun IncidentCard(incident: Incident, onResolveClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = incident.type.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Badge(containerColor = if (incident.priority == "high" || incident.priority == "critical") Color.Red else Color.Gray) {
                    Text(incident.priority.uppercase(), color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = incident.description ?: "No description", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onResolveClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Resolve")
            }
        }
    }
}
