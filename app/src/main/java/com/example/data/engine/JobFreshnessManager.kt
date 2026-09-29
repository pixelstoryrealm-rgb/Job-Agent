package com.example.data.engine

import com.example.data.model.Job
import java.util.concurrent.TimeUnit

enum class DeadlineReminderWindow(val daysThreshold: Int, val label: String) {
    SEVEN_DAYS(7, "7 days left"),
    THREE_DAYS(3, "3 days left"),
    ONE_DAY(1, "Deadline tomorrow")
}

class JobFreshnessManager {

    private val newThresholdMillis = TimeUnit.HOURS.toMillis(48) // New if first seen within 48h

    fun processFreshness(job: Job): Job {
        val now = System.currentTimeMillis()
        val isExpired = job.deadlineTimestamp != null && job.deadlineTimestamp < now
        val isNewJob = (now - job.firstSeenAt) <= newThresholdMillis

        return job.copy(
            active = !isExpired && job.active,
            isNew = isNewJob && !isExpired
        )
    }

    fun getDeadlineReminderUrgency(job: Job): DeadlineReminderWindow? {
        val deadline = job.deadlineTimestamp ?: return null
        val now = System.currentTimeMillis()
        if (deadline <= now) return null // Already expired

        val diffMillis = deadline - now
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return when {
            diffDays <= 1 -> DeadlineReminderWindow.ONE_DAY
            diffDays <= 3 -> DeadlineReminderWindow.THREE_DAYS
            diffDays <= 7 -> DeadlineReminderWindow.SEVEN_DAYS
            else -> null
        }
    }
}
