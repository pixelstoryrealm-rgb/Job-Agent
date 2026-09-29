package com.example.data.model

enum class NotificationType(val label: String) {
    NEW_MATCH("New Match"),
    HIGH_MATCH("High Match"),
    APPLICATION_UPDATE("Application Update"),
    INTERVIEW_REMINDER("Interview Reminder"),
    DEADLINE_ALERT("Deadline Reminder")
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: String,
    val isRead: Boolean = false,
    val targetJobId: String? = null,
    val targetApplicationId: String? = null
)
