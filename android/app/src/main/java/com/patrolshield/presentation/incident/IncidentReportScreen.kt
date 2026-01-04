package com.patrolshield.presentation.incident

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentReportScreen(
    siteId: Int,
    patrolRunId: Int? = null,
    onBack: () -> Unit,
    viewModel: IncidentViewModel = hiltViewModel()
) {
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Maintenance") }
    var priority by remember { mutableStateOf("Medium") }
    val isLoading = viewModel.isLoading.value
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = tempCameraUri
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is IncidentViewModel.IncidentEvent.Success -> {
                    onBack()
                }
                is IncidentViewModel.IncidentEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Report Incident") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Incident Type", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Security", "Maintenance", "Safety", "Medical", "Fire").forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = t },
                        label = { Text(t) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Priority", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Low", "Medium", "High", "Critical").forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(p) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Evidence", style = MaterialTheme.typography.labelLarge)
            
            if (selectedImageUri != null) {
                Image(
                    painter = coil.compose.rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Selected Evidence",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                TextButton(onClick = { selectedImageUri = null }) {
                    Text("Remove Image", color = MaterialTheme.colorScheme.error)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                com.patrolshield.common.ImageUtils.createTempImageFile(context)
                            )
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        }
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Camera")
                    }
                    
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            galleryLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { 
                    val compressedFile = selectedImageUri?.let { 
                        com.patrolshield.common.ImageUtils.compressImage(context, it) 
                    }
                    viewModel.reportIncident(type, priority.lowercase(), description, siteId, null, null, patrolRunId, compressedFile)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading && description.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                else Text("SUBMIT REPORT")
            }
        }
    }
}
