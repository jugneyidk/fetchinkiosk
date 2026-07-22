package com.fetchin.kiosk.web

import java.net.URI

class UrlPolicy(allowedHosts: Set<String>) {
    private val normalizedAllowedHosts = allowedHosts.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()

    fun isAllowed(rawUrl: String?): Boolean {
        return evaluate(rawUrl) == UrlPolicyDecision.Allowed
    }

    fun evaluate(rawUrl: String?): UrlPolicyDecision {
        if (rawUrl.isNullOrBlank()) return UrlPolicyDecision.Blocked(UrlBlockReason.Empty)

        val uri = runCatching { URI(rawUrl.trim()) }.getOrNull()
            ?: return UrlPolicyDecision.Blocked(UrlBlockReason.Invalid)
        val scheme = uri.scheme?.lowercase()
            ?: return UrlPolicyDecision.Blocked(UrlBlockReason.MissingScheme)
        if (scheme != "https") return UrlPolicyDecision.Blocked(UrlBlockReason.BlockedScheme)
        val host = uri.host?.lowercase()
            ?: return UrlPolicyDecision.Blocked(UrlBlockReason.MissingHost)
        val allowed = normalizedAllowedHosts.any { allowedHost ->
            host == allowedHost || host.endsWith(".$allowedHost")
        }
        return if (allowed) UrlPolicyDecision.Allowed else UrlPolicyDecision.Blocked(UrlBlockReason.BlockedHost)
    }
}

sealed interface UrlPolicyDecision {
    data object Allowed : UrlPolicyDecision
    data class Blocked(val reason: UrlBlockReason) : UrlPolicyDecision
}

enum class UrlBlockReason {
    Empty,
    Invalid,
    MissingScheme,
    BlockedScheme,
    MissingHost,
    BlockedHost
}
