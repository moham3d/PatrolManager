package com.patrolshield.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

@Composable
fun OfflineIndicator() {
    val context = LocalContext.current
    var isOffline by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        while (true) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            isOffline = capabilities == null || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            kotlinx.coroutines.delay(5000L)
        }
    }

    if (isOffline) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "OFFLINE MODE - Syncing suspended",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
