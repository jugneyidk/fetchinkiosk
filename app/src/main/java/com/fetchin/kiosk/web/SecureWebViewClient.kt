package com.fetchin.kiosk.web

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebResourceError
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fetchin.kiosk.ui.KioskUiState
import java.io.ByteArrayInputStream

class SecureWebViewClient(
    private val urlPolicy: UrlPolicy,
    private val onStateChanged: (KioskUiState) -> Unit,
    private val onRendererGone: (Boolean) -> Unit
) : WebViewClient() {
    private var mainFrameFailed = false

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
        mainFrameFailed = false
        if (!urlPolicy.isAllowed(url)) {
            mainFrameFailed = true
            view.stopLoading()
            onStateChanged(KioskUiState.BlockedNavigation)
            return
        }
        onStateChanged(KioskUiState.Loading)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        if (!mainFrameFailed && urlPolicy.isAllowed(url)) {
            onStateChanged(KioskUiState.WebContent)
        }
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        if (request.isForMainFrame) {
            mainFrameFailed = true
            onStateChanged(KioskUiState.LoadError(error.description?.toString().orEmpty()))
        }
    }

    override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
        if (request.isForMainFrame) {
            mainFrameFailed = true
            onStateChanged(KioskUiState.LoadError("HTTP ${errorResponse.statusCode}"))
        }
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        mainFrameFailed = true
        handler.cancel()
        onStateChanged(KioskUiState.LoadError("TLS certificate error"))
    }

    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        mainFrameFailed = true
        onRendererGone(detail.didCrash())
        return true
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
