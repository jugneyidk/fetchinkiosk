package com.fetchin.kiosk.admin

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class Pbkdf2AdminPinVerifierTest {
    @Test
    fun verifiesMatchingPinHash() {
        val salt = "0123456789abcdef".toByteArray()
        val hash = Pbkdf2AdminPinVerifier.deriveHash("123456".toCharArray(), salt, 1_000, 256)
        val verifier = Pbkdf2AdminPinVerifier(config(hash, salt))

        assertTrue(verifier.verify("123456".toCharArray()))
    }

    @Test
    fun rejectsWrongPin() {
        val salt = "0123456789abcdef".toByteArray()
        val hash = Pbkdf2AdminPinVerifier.deriveHash("123456".toCharArray(), salt, 1_000, 256)
        val verifier = Pbkdf2AdminPinVerifier(config(hash, salt))

        assertFalse(verifier.verify("000000".toCharArray()))
    }

    @Test
    fun rejectsWhenConfigMissing() {
        val verifier = Pbkdf2AdminPinVerifier(
            AdminPinConfig(hashBase64 = "", saltBase64 = "", iterations = 1_000, keyLengthBits = 256)
        )

        assertFalse(verifier.verify("123456".toCharArray()))
    }

    @Test
    fun clearsCandidateAfterVerification() {
        val salt = "0123456789abcdef".toByteArray()
        val hash = Pbkdf2AdminPinVerifier.deriveHash("123456".toCharArray(), salt, 1_000, 256)
        val verifier = Pbkdf2AdminPinVerifier(config(hash, salt))
        val candidate = "123456".toCharArray()

        verifier.verify(candidate)

        assertTrue(candidate.all { it == '\u0000' })
    }

    private fun config(hash: ByteArray, salt: ByteArray): AdminPinConfig {
        return AdminPinConfig(
            hashBase64 = Base64.getEncoder().encodeToString(hash),
            saltBase64 = Base64.getEncoder().encodeToString(salt),
            iterations = 1_000,
            keyLengthBits = 256
        )
    }
}
