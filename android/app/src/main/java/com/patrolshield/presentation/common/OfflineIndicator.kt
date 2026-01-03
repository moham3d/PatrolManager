package com.patrolshield.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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

@Composable
fun OfflineIndicator() {
    val context = LocalContext.current
    val connectionState by context.observeConnectivityAsFlow().collectAsState(initial = ConnectionState.Available)

    AnimatedVisibility(
        visible = connectionState == ConnectionState.Unavailable,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red.copy(alpha = 0.8f))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Offline Mode - Actions will sync later",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
