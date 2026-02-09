package com.patrolshield.presentation.site

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteListScreen(
    onBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: SiteViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Site Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, contentDescription = "Add Site")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                state.error != null -> {
                    Column(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadSites() }) { Text("Retry") }
                    }
                }
                else -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(state.sites) { site ->
                            SiteItem(site)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SiteItem(site: com.patrolshield.data.remote.dto.SiteDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        ListItem(
            headlineContent = { Text(site.name) },
            supportingContent = { Text(site.address ?: "No Address") },
            leadingContent = { Icon(Icons.Default.Place, contentDescription = null) }
        )
    }
}
