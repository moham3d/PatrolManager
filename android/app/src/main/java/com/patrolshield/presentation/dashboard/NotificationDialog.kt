package com.patrolshield.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.patrolshield.common.DateUtils
import com.patrolshield.data.local.entities.NotificationEntity

@Composable
fun NotificationDialog(
    notifications: List<NotificationEntity>,
    onDismiss: () -> Unit,
    onMarkAsRead: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f) // Take up 80% height
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = onClearAll) {
                            Text("Clear All")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No notifications", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(notifications) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = { onMarkAsRead(notification.id) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon based on type? For now just dot for unread
        if (!notification.isRead) {
            Badge(modifier = Modifier.padding(end = 8.dp))
        } else {
            Spacer(modifier = Modifier.width(16.dp)) // Spacer to align text
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (!notification.isRead) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
            Text(
                text = DateUtils.formatTime(notification.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
