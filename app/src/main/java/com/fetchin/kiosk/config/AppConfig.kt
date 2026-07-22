package com.fetchin.kiosk.config

import com.fetchin.kiosk.BuildConfig
import com.fetchin.kiosk.web.UrlPolicy

data class AppConfig(
    val startUrl: String,
    val allowedHosts: Set<String>,
    val allowScreenshots: Boolean,
    val webViewDebugging: Boolean,
    val adminGestureTapCount: Int,
    val adminGestureWindowMillis: Long,
    val userAgentSuffix: String?
) {
    fun urlPolicy(): UrlPolicy = UrlPolicy(allowedHosts)

    companion object {
        fun default(): AppConfig = AppConfig(
            startUrl = BuildConfig.DEFAULT_START_URL,
            allowedHosts = BuildConfig.DEFAULT_ALLOWED_HOSTS
                .split(',')
                .map { it.trim().lowercase() }
                .filter { it.isNotBlank() }
                .toSet(),
            allowScreenshots = false,
            webViewDebugging = BuildConfig.WEBVIEW_DEBUGGING,
            adminGestureTapCount = 7,
            adminGestureWindowMillis = 3_000L,
            userAgentSuffix = null
        )
    }
}
