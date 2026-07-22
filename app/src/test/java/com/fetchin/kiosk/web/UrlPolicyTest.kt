package com.fetchin.kiosk.web

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlPolicyTest {
    private val policy = UrlPolicy(setOf("pos.example.com", "api.pos.example.com"))

    @Test
    fun allowsExactAllowedHost() {
        assertTrue(policy.isAllowed("https://pos.example.com"))
        assertEquals(UrlPolicyDecision.Allowed, policy.evaluate("https://pos.example.com"))
    }

    @Test
    fun allowsSubdomainOfAllowedHost() {
        assertTrue(policy.isAllowed("https://sub.pos.example.com"))
    }

    @Test
    fun blocksHttp() {
        assertFalse(policy.isAllowed("http://pos.example.com"))
        assertBlocked("http://pos.example.com", UrlBlockReason.BlockedScheme)
    }

    @Test
    fun blocksLookalikeSuffixHost() {
        assertFalse(policy.isAllowed("https://pos.example.com.attacker.com"))
        assertBlocked("https://pos.example.com.attacker.com", UrlBlockReason.BlockedHost)
    }

    @Test
    fun blocksDifferentHost() {
        assertFalse(policy.isAllowed("https://example.com"))
    }

    @Test
    fun blocksFileScheme() {
        assertFalse(policy.isAllowed("file:///tmp/test"))
        assertBlocked("file:///tmp/test", UrlBlockReason.BlockedScheme)
    }

    @Test
    fun blocksContentScheme() {
        assertFalse(policy.isAllowed("content://settings"))
    }

    @Test
    fun blocksIntentScheme() {
        assertFalse(policy.isAllowed("intent://open"))
    }

    @Test
    fun blocksJavascriptScheme() {
        assertFalse(policy.isAllowed("javascript:alert(1)"))
    }

    @Test
    fun blocksEmptyUrl() {
        assertFalse(policy.isAllowed(""))
        assertBlocked("", UrlBlockReason.Empty)
    }

    @Test
    fun blocksInvalidUrl() {
        assertFalse(policy.isAllowed("not a url"))
        assertBlocked("not a url", UrlBlockReason.Invalid)
    }

    @Test
    fun blocksRelativeUrl() {
        assertFalse(policy.isAllowed("/orders"))
        assertBlocked("/orders", UrlBlockReason.MissingScheme)
    }

    @Test
    fun blocksHttpsWithoutHost() {
        assertFalse(policy.isAllowed("https:///orders"))
        assertBlocked("https:///orders", UrlBlockReason.MissingHost)
    }

    private fun assertBlocked(url: String, expectedReason: UrlBlockReason) {
        assertEquals(UrlPolicyDecision.Blocked(expectedReason), policy.evaluate(url))
    }
}
