package com.fetchin.kiosk.admin

import java.security.MessageDigest
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class Pbkdf2AdminPinVerifier(private val config: AdminPinConfig) : AdminPinVerifier {
    override fun verify(pinCandidate: CharArray): Boolean {
        return try {
            if (!config.isConfigured) return false
            val salt = Base64.getDecoder().decode(config.saltBase64)
            val expectedHash = Base64.getDecoder().decode(config.hashBase64)
            val actualHash = deriveHash(pinCandidate, salt, config.iterations, config.keyLengthBits)
            MessageDigest.isEqual(actualHash, expectedHash)
        } catch (_: IllegalArgumentException) {
            false
        } finally {
            pinCandidate.fill('\u0000')
        }
    }

    companion object {
        fun deriveHash(pin: CharArray, salt: ByteArray, iterations: Int, keyLengthBits: Int): ByteArray {
            val spec = PBEKeySpec(pin, salt, iterations, keyLengthBits)
            return try {
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            } finally {
                spec.clearPassword()
            }
        }
    }
}
