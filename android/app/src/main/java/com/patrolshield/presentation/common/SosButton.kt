package com.patrolshield.presentation.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SosButton(
    onTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressing by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(5) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isPressing) {
        if (isPressing) {
            countdown = 5
            while (countdown > 0 && isPressing) {
                delay(1000L)
                countdown--
            }
            if (countdown == 0 && isPressing) {
                onTrigger()
                isPressing = false
            }
        }
    }

    Box(
        modifier = modifier
            .size(if (isPressing) 120.dp else 72.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressing = true
                        tryAwaitRelease()
                        isPressing = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (isPressing) {
            CircularProgressIndicator(
                progress = countdown.toFloat() / 5f,
                modifier = Modifier.fillMaxSize(),
                color = Color.Red,
                strokeWidth = 8.dp
            )
            Text(
                text = countdown.toString(),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Red
            )
        } else {
            FloatingActionButton(
                onClick = {},
                containerColor = Color.Red,
                contentColor = Color.White,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(Icons.Default.Warning, contentDescription = "SOS", modifier = Modifier.size(36.dp))
            }
        }
    }
}
