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
import com.fetchin.kiosk.ui.KioskUiState
import com.fetchin.kiosk.web.SecureWebViewClient
import com.fetchin.kiosk.web.WebViewConfigurator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var config: AppConfig
    private lateinit var kioskController: KioskController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        config = AppConfig.default()
        kioskController = KioskController(this)
        AdminAccessController(config.adminGestureTapCount, config.adminGestureWindowMillis)

        applyScreenPolicy()
        configureBackBehavior()
        configureWebView()
        render(KioskUiState.Loading)
        binding.kioskWebView.loadUrl(config.startUrl)
        kioskController.startLockTaskIfAllowed()
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
            render(KioskUiState.Loading)
            binding.kioskWebView.reload()
        }
    }

    private fun render(state: KioskUiState) {
        binding.statusPanel.visibility = when (state) {
            KioskUiState.WebContent -> View.GONE
            else -> View.VISIBLE
        }
        binding.statusTitle.text = when (state) {
            KioskUiState.Initializing -> getString(R.string.state_initializing)
            KioskUiState.Loading -> getString(R.string.state_loading)
            KioskUiState.WebContent -> ""
            KioskUiState.Offline -> getString(R.string.state_offline)
            KioskUiState.LoadError -> getString(R.string.state_load_error)
            KioskUiState.BlockedNavigation -> getString(R.string.state_blocked_navigation)
            KioskUiState.NotProvisioned -> getString(R.string.state_not_provisioned)
            KioskUiState.Maintenance -> getString(R.string.state_maintenance)
        }
        binding.retryButton.visibility = when (state) {
            KioskUiState.Offline, KioskUiState.LoadError, KioskUiState.BlockedNavigation -> View.VISIBLE
            else -> View.GONE
        }
    }
}
