package com.fetchin.kiosk.config

import com.fetchin.kiosk.admin.Pbkdf2AdminPinVerifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InitialSetupConfigBuilderTest {
    @Test
    fun buildsConfigForHttpsUrlAndMatchingPin() {
        val result = builder().build(
            startUrlInput = "https://example.com/system",
            pin = "123456".toCharArray(),
            pinConfirmation = "123456".toCharArray()
        )

        assertTrue(result is InitialSetupResult.Success)
        val config = (result as InitialSetupResult.Success).config
        assertEquals("https://example.com/system", config.startUrl)
        assertEquals(setOf("example.com"), config.allowedHosts)
        assertTrue(Pbkdf2AdminPinVerifier(config.adminPinConfig).verify("123456".toCharArray()))
    }

    @Test
    fun rejectsHttpUrl() {
        val result = builder().build(
            startUrlInput = "http://example.com",
            pin = "123456".toCharArray(),
            pinConfirmation = "123456".toCharArray()
        )

        assertEquals(InitialSetupResult.NonHttpsUrl, result)
    }

    @Test
    fun rejectsMissingHost() {
        val result = builder().build(
            startUrlInput = "https:///system",
            pin = "123456".toCharArray(),
            pinConfirmation = "123456".toCharArray()
        )

        assertEquals(InitialSetupResult.MissingHost, result)
    }

    @Test
    fun rejectsMismatchedPinConfirmation() {
        val result = builder().build(
            startUrlInput = "https://example.com",
            pin = "123456".toCharArray(),
            pinConfirmation = "654321".toCharArray()
        )

        assertEquals(InitialSetupResult.PinMismatch, result)
    }

    @Test
    fun clearsPinCandidates() {
        val pin = "123456".toCharArray()
        val confirmation = "123456".toCharArray()

        builder().build("https://example.com", pin, confirmation)

        assertTrue(pin.all { it == '\u0000' })
        assertTrue(confirmation.all { it == '\u0000' })
    }

    private fun builder(): InitialSetupConfigBuilder {
        return InitialSetupConfigBuilder(iterations = 1_000, keyLengthBits = 256)
    }
}
