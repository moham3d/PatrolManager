package com.patrolshield.domain.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.local.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<NotificationEntity>>
    fun getUnreadCount(): Flow<Int>
    suspend fun markAsRead(id: Int)
    suspend fun markAllAsRead()
    suspend fun clearAll()
    // For simulation/testing:
    suspend fun createSampleNotification()
}
