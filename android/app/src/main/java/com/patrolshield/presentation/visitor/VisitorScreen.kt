package com.patrolshield.presentation.visitor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrolshield.data.remote.dto.VisitorDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorScreen(
    onBack: () -> Unit,
    viewModel: VisitorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visitor Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && state.visitors.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadVisitors() }) {
                            Text("Retry")
                        }
                    }
                }
                state.visitors.isEmpty() -> {
                    Text(
                        "No visitors scheduled for today.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.visitors) { visitor ->
                            VisitorItem(
                                visitor = visitor,
                                onCheckIn = { viewModel.checkInVisitor(visitor.id) }
                            )
                        }
                    }
                }
            }
            
            if (state.isLoading && state.visitors.isNotEmpty()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
fun VisitorItem(
    visitor: VisitorDto,
    onCheckIn: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = visitor.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = visitor.expectedArrivalTime.substringAfter("T").take(5), // Simple time extract
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Host: ${visitor.hostName}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Purpose: ${visitor.purpose}", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (visitor.status == "expected") {
                Button(
                    onClick = onCheckIn,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check In")
                }
            } else {
                 OutlinedButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (visitor.status) {
                            "checked_in" -> "Checked In"
                            "checked_out" -> "Checked Out"
                            else -> visitor.status
                        }
                    )
                }
            }
        }
    }
}
