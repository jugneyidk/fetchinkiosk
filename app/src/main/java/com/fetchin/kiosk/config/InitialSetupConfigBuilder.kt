package com.fetchin.kiosk.config

import com.fetchin.kiosk.BuildConfig
import com.fetchin.kiosk.admin.AdminPinConfig
import com.fetchin.kiosk.admin.Pbkdf2AdminPinVerifier
import java.net.URI
import java.security.SecureRandom
import java.util.Base64

class InitialSetupConfigBuilder(
    private val iterations: Int,
    private val keyLengthBits: Int,
    private val secureRandom: SecureRandom = SecureRandom()
) {
    fun build(startUrlInput: String, pin: CharArray, pinConfirmation: CharArray): InitialSetupResult {
        return try {
            val uri = runCatching { URI(startUrlInput.trim()) }.getOrNull() ?: return InitialSetupResult.InvalidUrl
            if (!uri.scheme.equals("https", ignoreCase = true)) return InitialSetupResult.NonHttpsUrl
            val host = uri.host?.lowercase() ?: return InitialSetupResult.MissingHost
            if (pin.isEmpty()) return InitialSetupResult.EmptyPin
            if (!pin.contentEquals(pinConfirmation)) return InitialSetupResult.PinMismatch

            val salt = ByteArray(SALT_BYTES)
            secureRandom.nextBytes(salt)
            val hash = Pbkdf2AdminPinVerifier.deriveHash(pin, salt, iterations, keyLengthBits)
            val encoder = Base64.getEncoder()
            val config = AppConfig.local(
                startUrl = uri.toString(),
                allowedHosts = setOf(host),
                adminPinConfig = AdminPinConfig(
                    hashBase64 = encoder.encodeToString(hash),
                    saltBase64 = encoder.encodeToString(salt),
                    iterations = iterations,
                    keyLengthBits = keyLengthBits
                )
            )
            InitialSetupResult.Success(config)
        } finally {
            pin.fill('\u0000')
            pinConfirmation.fill('\u0000')
        }
    }

    companion object {
        private const val SALT_BYTES = 16

        fun default(): InitialSetupConfigBuilder = InitialSetupConfigBuilder(
            iterations = BuildConfig.DEFAULT_ADMIN_PIN_ITERATIONS,
            keyLengthBits = BuildConfig.DEFAULT_ADMIN_PIN_KEY_LENGTH_BITS
        )
    }
}

sealed interface InitialSetupResult {
    data class Success(val config: AppConfig) : InitialSetupResult
    data object InvalidUrl : InitialSetupResult
    data object NonHttpsUrl : InitialSetupResult
    data object MissingHost : InitialSetupResult
    data object EmptyPin : InitialSetupResult
    data object PinMismatch : InitialSetupResult
}
