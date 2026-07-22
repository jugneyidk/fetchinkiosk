package com.fetchin.kiosk

import android.os.Bundle
import android.view.View
import android.view.WindowManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        config = AppConfig.default()
        kioskController = KioskController(this)
        connectivityObserver = AndroidConnectivityObserver(this)
        AdminAccessController(config.adminGestureTapCount, config.adminGestureWindowMillis)

        applyScreenPolicy()
        configureBackBehavior()
        configureWebView()
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
                    if (binding.kioskWebView.canGoBack()) {
                        binding.kioskWebView.goBack()
                    } else {
                        render(KioskUiState.WebContent)
                    }
                }
            }
        )
    }

    private fun configureWebView() {
        val urlPolicy = config.urlPolicy()
        WebViewConfigurator(config).configure(binding.kioskWebView)
        binding.kioskWebView.webViewClient = SecureWebViewClient(urlPolicy) { render(it) }
        binding.kioskWebView.setDownloadListener { _, _, _, _, _ ->
            render(KioskUiState.BlockedNavigation)
        }
        binding.retryButton.setOnClickListener {
            loadConfiguredSystem()
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
        binding.kioskWebView.loadUrl(config.startUrl)
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
            KioskUiState.Maintenance -> getString(R.string.state_maintenance)
        }
        binding.statusMessage.text = when (state) {
            is KioskUiState.Offline -> state.detail
            is KioskUiState.LoadError -> state.detail.ifBlank { getString(R.string.state_load_error_detail) }
            is KioskUiState.NotProvisioned -> state.detail
            else -> ""
        }
        binding.statusMessage.visibility = when (state) {
            is KioskUiState.Offline, is KioskUiState.LoadError, is KioskUiState.NotProvisioned -> View.VISIBLE
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
