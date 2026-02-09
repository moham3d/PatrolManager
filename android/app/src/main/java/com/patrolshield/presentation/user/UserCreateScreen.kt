package com.patrolshield.presentation.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCreateScreen(
    onBack: () -> Unit,
    viewModel: CreateUserViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val roles = listOf(
        "Guard" to 1,
        "Supervisor" to 2,
        "Manager" to 3,
        "Admin" to 4
    )
    
    LaunchedEffect(state.success) {
        if (state.success) {
            onBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create User") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedRole?.first ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.first) },
                                onClick = {
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Button(
                    onClick = {
                        if (selectedRole != null) {
                            viewModel.createUser(name, email, password, selectedRole!!.second)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && selectedRole != null
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("CREATE USER")
                    }
                }
                
                if (state.error != null) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
