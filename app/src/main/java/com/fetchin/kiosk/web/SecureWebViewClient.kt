package com.fetchin.kiosk.web

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fetchin.kiosk.ui.KioskUiState
import java.io.ByteArrayInputStream

class SecureWebViewClient(
    private val urlPolicy: UrlPolicy,
    private val onStateChanged: (KioskUiState) -> Unit
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val decision = urlPolicy.evaluate(request.url.toString())
        if (decision is UrlPolicyDecision.Blocked) {
            if (request.isForMainFrame) {
                onStateChanged(KioskUiState.BlockedNavigation)
            }
            return true
        }
        return false
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        return if (urlPolicy.isAllowed(request.url.toString())) null else blockedResponse()
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        if (!urlPolicy.isAllowed(url)) {
            view.stopLoading()
            onStateChanged(KioskUiState.BlockedNavigation)
            return
        }
        onStateChanged(KioskUiState.Loading)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        onStateChanged(KioskUiState.WebContent)
    }

    private fun blockedResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            403,
            "Forbidden",
            emptyMap(),
            ByteArrayInputStream(ByteArray(0))
        )
    }
}
