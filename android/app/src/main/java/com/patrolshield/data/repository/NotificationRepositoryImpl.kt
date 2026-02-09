package com.patrolshield.data.repository

import com.patrolshield.data.local.dao.NotificationDao
import com.patrolshield.data.local.entities.NotificationEntity
import com.patrolshield.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao
) : NotificationRepository {

    override fun getNotifications(): Flow<List<NotificationEntity>> {
        return dao.getAllNotifications()
    }

    override fun getUnreadCount(): Flow<Int> {
        return dao.getUnreadCount()
    }

    override suspend fun markAsRead(id: Int) {
        dao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        dao.markAllAsRead()
    }
    
    override suspend fun clearAll() {
        dao.clearAll()
    }

    override suspend fun createSampleNotification() {
        dao.insertNotification(
            NotificationEntity(
                title = "System Alert",
                message = "Check-in required! You have not scanned a checkpoint in 15 minutes.",
                type = "alert"
            )
        )
    }
}
