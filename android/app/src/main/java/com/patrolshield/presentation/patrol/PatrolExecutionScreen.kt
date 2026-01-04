package com.patrolshield.presentation.patrol

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrolshield.presentation.common.SosButton
import com.patrolshield.common.LocationUtils
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatrolExecutionScreen(
    patrolId: Int,
    runId: Int,
    onFinish: () -> Unit,
    viewModel: PatrolExecutionViewModel = hiltViewModel()
) {
    val checkpoints = viewModel.checkpoints.value
    val currentIndex = viewModel.currentIndex.value
    val isLoading = viewModel.isLoading.value
    val context = LocalContext.current
    var showScanner by remember { mutableStateOf(false) }

    if (showScanner) {
        CheckpointScannerScreen(
            onScan = { code ->
                showScanner = false
                viewModel.handleScan("QR", code)
            },
            onClose = {
                showScanner = false
            }
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(title = { Text("Patrol in Progress") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                LinearProgressIndicator(
                    progress = if (checkpoints.isNotEmpty()) currentIndex.toFloat() / checkpoints.size else 0f,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Next Checkpoint:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                val nextCheckpoint = checkpoints.getOrNull(currentIndex)
                if (nextCheckpoint != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = nextCheckpoint.name, style = MaterialTheme.typography.headlineMedium)
                            Text(text = "Type: ${nextCheckpoint.type}", style = MaterialTheme.typography.bodyLarge)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (nextCheckpoint.type == "QR") {
                                Button(
                                    onClick = { showScanner = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Scan QR Code")
                                }
                            } else if (nextCheckpoint.type == "NFC") {
                                Text(
                                    text = "Tap NFC Tag to Scan",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
    
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Checkpoint Progress", style = MaterialTheme.typography.titleMedium)
                
                Column(modifier = Modifier.weight(1f)) {
                    checkpoints.forEachIndexed { index, checkpoint ->
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (index < currentIndex) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (index < currentIndex) Color.Green else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = checkpoint.name,
                                style = if (index == currentIndex) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                color = if (index == currentIndex) MaterialTheme.colorScheme.primary else Color.Unspecified
                            )
                        }
                    }
                }
    
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
            
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
                SosButton(onTrigger = {
                    val loc = LocationUtils.getLocation(context)
                    viewModel.triggerPanic(loc?.latitude ?: 0.0, loc?.longitude ?: 0.0)
                })
            }
        }
    }
}
