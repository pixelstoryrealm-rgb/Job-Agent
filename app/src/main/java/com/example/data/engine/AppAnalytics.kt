package com.example.data.engine

import android.util.Log

interface AppAnalytics {
    var isEnabled: Boolean
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())

    fun logJobViewed(jobId: String, title: String) =
        logEvent("job_viewed", mapOf("job_id" to jobId, "job_title" to title))

    fun logJobSaved(jobId: String) =
        logEvent("job_saved", mapOf("job_id" to jobId))

    fun logJobSkipped(jobId: String) =
        logEvent("job_skipped", mapOf("job_id" to jobId))

    fun logApplicationStarted(jobId: String, company: String) =
        logEvent("application_started", mapOf("job_id" to jobId, "company" to company))

    fun logApplicationStatusChanged(appId: String, newStatus: String) =
        logEvent("application_status_changed", mapOf("app_id" to appId, "status" to newStatus))

    fun logSearchPerformed(query: String, isAiParsed: Boolean) =
        logEvent("search_performed", mapOf("query_length" to query.length, "is_ai_parsed" to isAiParsed))

    fun logFilterUsed(filterName: String) =
        logEvent("filter_used", mapOf("filter_name" to filterName))

    fun logAiMatchViewed(jobId: String, matchPercent: Int) =
        logEvent("ai_match_viewed", mapOf("job_id" to jobId, "match_percentage" to matchPercent))
}

class PrivacyConsciousAnalytics : AppAnalytics {
    override var isEnabled: Boolean = true

    override fun logEvent(name: String, params: Map<String, Any?>) {
        if (!isEnabled) return
        // Privacy principle: never log PII (no CV text, phone, email)
        val sanitized = params.filterKeys { key ->
            key != "email" && key != "phone" && key != "cv" && key != "notes"
        }
        Log.d("AppAnalytics", "Event logged: $name -> $sanitized")
    }
}
