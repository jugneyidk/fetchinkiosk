package com.fetchin.kiosk

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.fetchin.kiosk.admin.AdminAccessController
import com.fetchin.kiosk.config.AppConfig
import com.fetchin.kiosk.databinding.ActivityMainBinding
import com.fetchin.kiosk.kiosk.KioskController
import com.fetchin.kiosk.kiosk.KioskProvisioningStatus
import com.fetchin.kiosk.kiosk.KioskStartResult
import com.fetchin.kiosk.ui.KioskUiState
import com.fetchin.kiosk.util.AndroidConnectivityObserver
import com.fetchin.kiosk.util.ConnectivityObserver
import com.fetchin.kiosk.web.SecureWebViewClient
import com.fetchin.kiosk.web.WebViewConfigurator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var config: AppConfig
    private lateinit var kioskController: KioskController
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var adminAccessController: AdminAccessController
    private lateinit var currentWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentWebView = binding.kioskWebView

        config = AppConfig.default()
        kioskController = KioskController(this)
        connectivityObserver = AndroidConnectivityObserver(this)
        adminAccessController = AdminAccessController(config.adminGestureTapCount, config.adminGestureWindowMillis)

        applyScreenPolicy()
        configureBackBehavior()
        configureWebView()
        configureAdminGesture()
        handleKioskStart(kioskController.startLockTaskIfAllowed())
    }

    override fun onResume() {
        super.onResume()
        enterImmersiveMode()
    }

    private fun applyScreenPolicy() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (!config.allowScreenshots) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        enterImmersiveMode()
    }

    private fun enterImmersiveMode() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
    }

    private fun configureBackBehavior() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (currentWebView.canGoBack()) {
                        currentWebView.goBack()
                    } else {
                        render(KioskUiState.WebContent)
                    }
                }
            }
        )
    }

    private fun configureWebView() {
        configureWebView(currentWebView)
        binding.retryButton.setOnClickListener {
            loadConfiguredSystem()
        }
    }

    private fun configureWebView(webView: WebView) {
        val urlPolicy = config.urlPolicy()
        WebViewConfigurator(config).configure(webView)
        webView.webViewClient = SecureWebViewClient(
            urlPolicy = urlPolicy,
            onStateChanged = { render(it) },
            onRendererGone = { recoverWebViewRenderer(it) }
        )
        webView.setDownloadListener { _, _, _, _, _ ->
            render(KioskUiState.BlockedNavigation)
        }
    }

    private fun configureAdminGesture() {
        binding.adminGestureTarget.setOnClickListener {
            if (adminAccessController.recordTap(SystemClock.elapsedRealtime())) {
                adminAccessController.reset()
                render(KioskUiState.AdminChallenge(getString(R.string.state_admin_challenge_detail)))
            }
        }
    }

    private fun handleKioskStart(result: KioskStartResult) {
        when (result) {
            is KioskStartResult.Started -> {
                binding.provisioningBanner.visibility = View.GONE
                loadConfiguredSystem()
            }
            is KioskStartResult.NotPermitted -> handleUnprovisioned(result.status)
            is KioskStartResult.Failed -> handleUnprovisioned(result.status)
        }
    }

    private fun handleUnprovisioned(status: KioskProvisioningStatus) {
        val detail = provisioningDetail(status)
        if (BuildConfig.DEBUG) {
            binding.provisioningBanner.text = getString(R.string.development_fallback_warning, detail)
            binding.provisioningBanner.visibility = View.VISIBLE
            loadConfiguredSystem()
        } else {
            binding.provisioningBanner.visibility = View.GONE
            render(KioskUiState.NotProvisioned(detail))
        }
    }

    private fun loadConfiguredSystem() {
        if (!connectivityObserver.isOnline()) {
            render(KioskUiState.Offline(getString(R.string.state_offline_detail)))
            return
        }
        render(KioskUiState.Loading)
        currentWebView.loadUrl(config.startUrl)
    }

    private fun recoverWebViewRenderer(didCrash: Boolean) {
        recreateWebView()
        val detail = if (didCrash) {
            getString(R.string.state_renderer_crash_detail)
        } else {
            getString(R.string.state_renderer_gone_detail)
        }
        render(KioskUiState.LoadError(detail))
    }

    private fun recreateWebView() {
        val oldWebView = currentWebView
        val replacement = WebView(this)
        replacement.id = R.id.kioskWebView
        replacement.layoutParams = oldWebView.layoutParams
        binding.root.removeView(oldWebView)
        oldWebView.stopLoading()
        oldWebView.setDownloadListener(null)
        oldWebView.destroy()
        currentWebView = replacement
        configureWebView(replacement)
        binding.root.addView(replacement, 0)
    }

    private fun render(state: KioskUiState) {
        binding.statusPanel.visibility = when (state) {
            KioskUiState.WebContent -> View.GONE
            else -> View.VISIBLE
        }
        binding.statusProgress.visibility = when (state) {
            KioskUiState.Initializing, KioskUiState.Loading -> View.VISIBLE
            else -> View.GONE
        }
        binding.statusTitle.text = when (state) {
            KioskUiState.Initializing -> getString(R.string.state_initializing)
            KioskUiState.Loading -> getString(R.string.state_loading)
            KioskUiState.WebContent -> ""
            is KioskUiState.Offline -> getString(R.string.state_offline)
            is KioskUiState.LoadError -> getString(R.string.state_load_error)
            KioskUiState.BlockedNavigation -> getString(R.string.state_blocked_navigation)
            is KioskUiState.NotProvisioned -> getString(R.string.state_not_provisioned)
            is KioskUiState.AdminChallenge -> getString(R.string.state_admin_challenge)
            KioskUiState.Maintenance -> getString(R.string.state_maintenance)
        }
        binding.statusMessage.text = when (state) {
            is KioskUiState.Offline -> state.detail
            is KioskUiState.LoadError -> state.detail.ifBlank { getString(R.string.state_load_error_detail) }
            is KioskUiState.NotProvisioned -> state.detail
            is KioskUiState.AdminChallenge -> state.detail
            else -> ""
        }
        binding.statusMessage.visibility = when (state) {
            is KioskUiState.Offline, is KioskUiState.LoadError, is KioskUiState.NotProvisioned, is KioskUiState.AdminChallenge -> View.VISIBLE
            else -> View.GONE
        }
        binding.retryButton.visibility = when (state) {
            is KioskUiState.Offline, is KioskUiState.LoadError, KioskUiState.BlockedNavigation -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun provisioningDetail(status: KioskProvisioningStatus): String {
        return getString(
            R.string.provisioning_detail,
            status.packageName,
            yesNo(status.isDeviceOwner),
            yesNo(status.isLockTaskPermitted)
        )
    }

    private fun yesNo(value: Boolean): String {
        return if (value) getString(R.string.value_yes) else getString(R.string.value_no)
    }
}
