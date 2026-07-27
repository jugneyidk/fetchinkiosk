package com.fetchin.kiosk.config

import com.fetchin.kiosk.BuildConfig
import com.fetchin.kiosk.admin.AdminPinConfig
import com.fetchin.kiosk.admin.AdminPinVerifier
import com.fetchin.kiosk.admin.Pbkdf2AdminPinVerifier
import com.fetchin.kiosk.web.UrlPolicy

data class AppConfig(
    val startUrl: String,
    val allowedHosts: Set<String>,
    val allowScreenshots: Boolean,
    val webViewDebugging: Boolean,
    val adminGestureTapCount: Int,
    val adminGestureWindowMillis: Long,
    val adminPinConfig: AdminPinConfig,
    val adminSessionMillis: Long,
    val userAgentSuffix: String?
) {
    fun urlPolicy(): UrlPolicy = UrlPolicy(allowedHosts)

    fun adminPinVerifier(): AdminPinVerifier = Pbkdf2AdminPinVerifier(adminPinConfig)

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
            adminPinConfig = AdminPinConfig(
                hashBase64 = BuildConfig.DEFAULT_ADMIN_PIN_HASH_BASE64,
                saltBase64 = BuildConfig.DEFAULT_ADMIN_PIN_SALT_BASE64,
                iterations = BuildConfig.DEFAULT_ADMIN_PIN_ITERATIONS,
                keyLengthBits = BuildConfig.DEFAULT_ADMIN_PIN_KEY_LENGTH_BITS
            ),
            adminSessionMillis = BuildConfig.DEFAULT_ADMIN_SESSION_MILLIS,
            userAgentSuffix = null
        )
    }
}
