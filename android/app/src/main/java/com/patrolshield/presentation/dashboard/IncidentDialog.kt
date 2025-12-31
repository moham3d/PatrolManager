package com.patrolshield.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.patrolshield.presentation.patrol.getLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentDialog(
    onDismiss: () -> Unit,
    onSubmit: (type: String, priority: String, description: String, lat: Double?, lng: Double?) -> Unit
) {
    var type by remember { mutableStateOf("Maintenance") }
    var priority by remember { mutableStateOf("Medium") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    val types = listOf("Security", "Maintenance", "Safety", "Other")
    val priorities = listOf("Low", "Medium", "High", "Critical")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Incident") },
        text = {
            Column {
                // Type Dropdown (Simplified as Radio/Text for now or standard exposed dropdown)
                // For MVP speed, let's use a simple row of chips or radio
                Text("Type:", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    types.take(2).forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                     types.drop(2).forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Priority:", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    priorities.forEach { p ->
                        // Just showing 4 might be tight, let's pick simplified
                        FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(p.take(1)) }) // L, M, H, C
                    }
                }
                 Text("Selected Priority: $priority", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSubmitting = true
                    // Capture location
                    val loc = getLocation(context)
                    if (loc != null) {
                        onSubmit(type, priority.lowercase(), description, loc.latitude, loc.longitude)
                    } else {
                         onSubmit(type, priority.lowercase(), description, null, null)
                    }
                    isSubmitting = false // dialog will likely dismiss before this re-renders matter
                },
                enabled = description.isNotBlank() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("SUBMIT")
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
