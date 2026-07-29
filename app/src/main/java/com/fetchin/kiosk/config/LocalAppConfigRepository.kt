package com.fetchin.kiosk.config

import android.content.Context
import com.fetchin.kiosk.admin.AdminPinConfig

class LocalAppConfigRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): AppConfig? {
        val startUrl = preferences.getString(KEY_START_URL, null)?.takeIf { it.isNotBlank() } ?: return null
        val allowedHost = preferences.getString(KEY_ALLOWED_HOST, null)?.takeIf { it.isNotBlank() } ?: return null
        val hashBase64 = preferences.getString(KEY_ADMIN_PIN_HASH, null)?.takeIf { it.isNotBlank() } ?: return null
        val saltBase64 = preferences.getString(KEY_ADMIN_PIN_SALT, null)?.takeIf { it.isNotBlank() } ?: return null
        val iterations = preferences.getInt(KEY_ADMIN_PIN_ITERATIONS, 0)
        val keyLengthBits = preferences.getInt(KEY_ADMIN_PIN_KEY_LENGTH_BITS, 0)
        return AppConfig.local(
            startUrl = startUrl,
            allowedHosts = setOf(allowedHost.lowercase()),
            adminPinConfig = AdminPinConfig(
                hashBase64 = hashBase64,
                saltBase64 = saltBase64,
                iterations = iterations,
                keyLengthBits = keyLengthBits
            )
        )
    }

    fun save(config: AppConfig) {
        val adminPinConfig = config.adminPinConfig
        require(config.startUrl.isNotBlank())
        require(config.allowedHosts.size == 1)
        require(adminPinConfig.isConfigured)
        preferences.edit()
            .putString(KEY_START_URL, config.startUrl)
            .putString(KEY_ALLOWED_HOST, config.allowedHosts.single())
            .putString(KEY_ADMIN_PIN_HASH, adminPinConfig.hashBase64)
            .putString(KEY_ADMIN_PIN_SALT, adminPinConfig.saltBase64)
            .putInt(KEY_ADMIN_PIN_ITERATIONS, adminPinConfig.iterations)
            .putInt(KEY_ADMIN_PIN_KEY_LENGTH_BITS, adminPinConfig.keyLengthBits)
            .apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "fetchin_kiosk_config"
        private const val KEY_START_URL = "start_url"
        private const val KEY_ALLOWED_HOST = "allowed_host"
        private const val KEY_ADMIN_PIN_HASH = "admin_pin_hash"
        private const val KEY_ADMIN_PIN_SALT = "admin_pin_salt"
        private const val KEY_ADMIN_PIN_ITERATIONS = "admin_pin_iterations"
        private const val KEY_ADMIN_PIN_KEY_LENGTH_BITS = "admin_pin_key_length_bits"
    }
}
