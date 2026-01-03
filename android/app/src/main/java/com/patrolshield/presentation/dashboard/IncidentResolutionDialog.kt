package com.patrolshield.presentation.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentResolutionDialog(
    incidentId: Int,
    onDismiss: () -> Unit,
    onSubmit: (notes: String, evidenceUri: Uri?) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImage = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Resolve Incident #$incidentId") },
        text = {
            Column {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Resolution Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Evidence (Optional):", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Image, contentDescription = "Gallery")
                    }
                }

                selectedImage?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSubmitting = true
                    onSubmit(notes, selectedImage)
                },
                enabled = notes.isNotBlank() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("RESOLVE")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
