package com.fetchin.kiosk.web

import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import com.fetchin.kiosk.config.AppConfig

class WebViewConfigurator(private val config: AppConfig) {
    fun configure(webView: WebView) {
        WebView.setWebContentsDebuggingEnabled(config.webViewDebugging)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = false
            allowFileAccess = false
            allowContentAccess = false
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            mediaPlaybackRequiresUserGesture = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true
            }
            userAgentString = buildUserAgent(userAgentString)
        }
    }

    private fun buildUserAgent(base: String): String {
        val suffix = config.userAgentSuffix ?: return base
        return "$base $suffix"
    }
}
