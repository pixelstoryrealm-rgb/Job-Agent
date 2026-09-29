package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val targetJobId: String? = null,
    val targetApplicationId: String? = null
) {
    fun toDomain(): NotificationItem {
        val notifType = try {
            NotificationType.valueOf(type)
        } catch (_: Exception) {
            NotificationType.NEW_MATCH
        }
        return NotificationItem(
            id = id,
            title = title,
            message = message,
            type = notifType,
            timestamp = timestamp,
            isRead = isRead,
            targetJobId = targetJobId,
            targetApplicationId = targetApplicationId
        )
    }

    companion object {
        fun fromDomain(item: NotificationItem): NotificationEntity {
            return NotificationEntity(
                id = item.id,
                title = item.title,
                message = item.message,
                type = item.type.name,
                timestamp = item.timestamp,
                isRead = item.isRead,
                targetJobId = item.targetJobId,
                targetApplicationId = item.targetApplicationId
            )
        }
    }
}
