package com.example.data.repository

import com.example.data.local.NotificationDao
import com.example.data.local.NotificationEntity
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface NotificationRepository {
    fun getNotifications(): Flow<List<NotificationItem>>
    fun getUnreadCount(): Flow<Int>
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(id: String)
    suspend fun seedInitialNotifications()
}

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override fun getNotifications(): Flow<List<NotificationItem>> {
        return notificationDao.getAllNotifications().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getUnreadCount(): Flow<Int> {
        return notificationDao.getUnreadCount()
    }

    override suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    override suspend fun deleteNotification(id: String) {
        notificationDao.deleteNotification(id)
    }

    override suspend fun seedInitialNotifications() {
        val initial = listOf(
            NotificationEntity(
                id = "notif_1",
                title = "High AI Match (92%) Found!",
                message = "Junior Maintenance Engineer at ABC Engineering Ltd. matches your Diploma in EEE.",
                type = NotificationType.HIGH_MATCH.name,
                timestamp = "10 mins ago",
                isRead = false,
                targetJobId = "job_1"
            ),
            NotificationEntity(
                id = "notif_2",
                title = "Govt Project Opening",
                message = "Assistant Engineer at Desh Power (Govt Partner) just posted in Dhaka.",
                type = NotificationType.NEW_MATCH.name,
                timestamp = "2 hours ago",
                isRead = false,
                targetJobId = "job_5"
            ),
            NotificationEntity(
                id = "notif_3",
                title = "Application Reviewed",
                message = "Meghna Power has reviewed your electrical profile for Junior Electrical Engineer.",
                type = NotificationType.APPLICATION_UPDATE.name,
                timestamp = "Yesterday",
                isRead = true,
                targetJobId = "job_2"
            )
        )
        notificationDao.insertNotifications(initial)
    }
}
