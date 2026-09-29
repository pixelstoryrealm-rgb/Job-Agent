package com.example.data.engine

import android.net.Uri

data class UrlSafetyCheckResult(
    val isValid: Boolean,
    val isHttps: Boolean,
    val host: String?,
    val sanitizedUrl: String?,
    val warningMessage: String? = null
)

class UrlSafetyValidator {

    fun validateAndSanitizeUrl(rawUrl: String?): UrlSafetyCheckResult {
        if (rawUrl.isNullOrBlank()) {
            return UrlSafetyCheckResult(
                isValid = false,
                isHttps = false,
                host = null,
                sanitizedUrl = null,
                warningMessage = "Application link is not specified"
            )
        }

        val trimmed = rawUrl.trim()
        val uri = try {
            Uri.parse(trimmed)
        } catch (_: Exception) {
            return UrlSafetyCheckResult(
                isValid = false,
                isHttps = false,
                host = null,
                sanitizedUrl = null,
                warningMessage = "Malformed URL format"
            )
        }

        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") {
            return UrlSafetyCheckResult(
                isValid = false,
                isHttps = false,
                host = null,
                sanitizedUrl = null,
                warningMessage = "Unsupported link scheme ($scheme). Only HTTP/HTTPS allowed."
            )
        }

        val host = uri.host
        if (host.isNullOrBlank()) {
            return UrlSafetyCheckResult(
                isValid = false,
                isHttps = scheme == "https",
                host = null,
                sanitizedUrl = null,
                warningMessage = "Missing destination domain"
            )
        }

        // Check for deceptive URL patterns (e.g. user info spoofing like http://google.com@evil.com)
        if (uri.userInfo != null) {
            return UrlSafetyCheckResult(
                isValid = false,
                isHttps = scheme == "https",
                host = host,
                sanitizedUrl = null,
                warningMessage = "Deceptive URL detected (embedded credentials)"
            )
        }

        val isHttps = scheme == "https"
        val warning = if (!isHttps) "Connection is HTTP (not secure)" else null

        return UrlSafetyCheckResult(
            isValid = true,
            isHttps = isHttps,
            host = host,
            sanitizedUrl = trimmed,
            warningMessage = warning
        )
    }
}
