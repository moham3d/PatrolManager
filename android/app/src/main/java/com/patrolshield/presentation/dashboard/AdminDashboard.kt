package com.patrolshield.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onLogout: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToSites: () -> Unit,
    viewModel: AdminDashboardViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadStats() }) {
                            Text("Retry")
                        }
                    }
                }
                state.stats != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Quick Stats
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            StatsCard(
                                title = "Total Users",
                                count = state.stats.totalUsers.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                title = "Active Users",
                                count = state.stats.activeUsers.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text("Role Distribution", style = MaterialTheme.typography.titleMedium)
                        
                        // Role List
                        state.stats.roleCounts.forEach { (role, count) ->
                            ListItem(
                                headlineContent = { Text(role.uppercase()) },
                                trailingContent = { Text(count.toString()) },
                                leadingContent = { Icon(Icons.Default.Person, contentDescription = null) }
                            )
                            HorizontalDivider()
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Management Buttons
                        Button(
                            onClick = onNavigateToUsers,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("MANAGE USERS")
                        }
                        
                        Button(
                            onClick = onNavigateToSites,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("MANAGE SITES")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(title: String, count: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(count, style = MaterialTheme.typography.displayMedium)
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
