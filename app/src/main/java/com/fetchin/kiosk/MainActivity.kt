package com.fetchin.kiosk

import android.text.InputType
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.fetchin.kiosk.admin.AdminAccessController
import com.fetchin.kiosk.admin.AdminPinVerifier
import com.fetchin.kiosk.config.AppConfig
import com.fetchin.kiosk.config.InitialSetupConfigBuilder
import com.fetchin.kiosk.config.InitialSetupResult
import com.fetchin.kiosk.config.LocalAppConfigRepository
import com.fetchin.kiosk.databinding.ActivityMainBinding
import com.fetchin.kiosk.kiosk.KioskController
import com.fetchin.kiosk.kiosk.KioskProvisioningStatus
import com.fetchin.kiosk.kiosk.KioskStartResult
import com.fetchin.kiosk.kiosk.KioskStopResult
import com.fetchin.kiosk.security.KioskLogger
import com.fetchin.kiosk.ui.KioskUiState
import com.fetchin.kiosk.util.AndroidConnectivityObserver
import com.fetchin.kiosk.util.ConnectivityObserver
import com.fetchin.kiosk.web.SecureWebViewClient
import com.fetchin.kiosk.web.WebViewConfigurator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var config: AppConfig
    private lateinit var configRepository: LocalAppConfigRepository
    private lateinit var kioskController: KioskController
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var adminAccessController: AdminAccessController
    private lateinit var adminPinVerifier: AdminPinVerifier
    private lateinit var currentWebView: WebView
    private val kioskLogger = KioskLogger()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentUiState: KioskUiState = KioskUiState.Initializing
    private val maintenanceTimeoutRunnable = Runnable { endMaintenanceMode() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentWebView = binding.kioskWebView

        configRepository = LocalAppConfigRepository(this)
        kioskController = KioskController(this)
        connectivityObserver = AndroidConnectivityObserver(this)

        applyScreenPolicy()
        configureBackBehavior()
        configureInitialSetup()
        val storedConfig = configRepository.load()
        if (storedConfig == null) {
            showInitialSetup()
        } else {
            startKiosk(storedConfig)
        }
    }

    override fun onResume() {
        super.onResume()
        enterImmersiveMode()
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(maintenanceTimeoutRunnable)
        super.onDestroy()
    }

    private fun applyScreenPolicy() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
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
                    if (binding.setupPanel.visibility == View.VISIBLE) {
                        return
                    } else if (currentUiState is KioskUiState.Maintenance) {
                        endMaintenanceMode()
                    } else if (currentWebView.canGoBack()) {
                        currentWebView.goBack()
                    } else {
                        render(KioskUiState.WebContent)
                    }
                }
            }
        )
    }

    private fun configureInitialSetup() {
        binding.setupSaveButton.setOnClickListener {
            saveInitialSetup()
        }
    }

    private fun showInitialSetup() {
        binding.setupPanel.visibility = View.VISIBLE
        binding.kioskWebView.visibility = View.GONE
        binding.statusPanel.visibility = View.GONE
        binding.provisioningBanner.visibility = View.GONE
        binding.adminGestureTarget.visibility = View.GONE
    }

    private fun saveInitialSetup() {
        val result = InitialSetupConfigBuilder.default().build(
            startUrlInput = binding.setupStartUrlInput.text?.toString().orEmpty(),
            pin = editableToCharArray(binding.setupPinInput.text),
            pinConfirmation = editableToCharArray(binding.setupPinConfirmInput.text)
        )
        binding.setupPinInput.text?.clear()
        binding.setupPinConfirmInput.text?.clear()
        when (result) {
            is InitialSetupResult.Success -> {
                configRepository.save(result.config)
                binding.setupError.visibility = View.GONE
                startKiosk(result.config)
            }
            else -> showInitialSetupError(result)
        }
    }

    private fun showInitialSetupError(result: InitialSetupResult) {
        binding.setupError.text = when (result) {
            InitialSetupResult.InvalidUrl -> getString(R.string.setup_error_invalid_url)
            InitialSetupResult.NonHttpsUrl -> getString(R.string.setup_error_non_https_url)
            InitialSetupResult.MissingHost -> getString(R.string.setup_error_missing_host)
            InitialSetupResult.EmptyPin -> getString(R.string.setup_error_empty_pin)
            InitialSetupResult.PinMismatch -> getString(R.string.setup_error_pin_mismatch)
            is InitialSetupResult.Success -> ""
        }
        binding.setupError.visibility = View.VISIBLE
    }

    private fun startKiosk(storedConfig: AppConfig) {
        config = storedConfig
        adminAccessController = AdminAccessController(config.adminGestureTapCount, config.adminGestureWindowMillis)
        adminPinVerifier = config.adminPinVerifier()
        binding.setupPanel.visibility = View.GONE
        binding.kioskWebView.visibility = View.VISIBLE
        binding.adminGestureTarget.visibility = View.VISIBLE
        configureWebView()
        configureAdminGesture()
        handleKioskStart(kioskController.startLockTaskIfAllowed())
    }

    private fun configureWebView() {
        configureWebView(currentWebView)
        binding.retryButton.setOnClickListener {
            handlePrimaryAction()
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
                showAdminPinChallenge()
            }
        }
    }

    private fun showAdminPinChallenge() {
        val pinInput = androidx.appcompat.widget.AppCompatEditText(this)
        pinInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        pinInput.hint = getString(R.string.admin_pin_hint)
        AlertDialog.Builder(this)
            .setTitle(R.string.state_admin_challenge)
            .setMessage(R.string.state_admin_challenge_detail)
            .setView(pinInput)
            .setPositiveButton(R.string.action_verify) { _, _ ->
                verifyAdminPin(pinInput)
            }
            .setNegativeButton(R.string.action_cancel) { dialog, _ ->
                pinInput.text?.clear()
                dialog.dismiss()
            }
            .show()
    }

    private fun verifyAdminPin(pinInput: androidx.appcompat.widget.AppCompatEditText) {
        val candidate = editableToCharArray(pinInput.text)
        val verified = adminPinVerifier.verify(candidate)
        pinInput.text?.clear()
        if (verified) {
            enterMaintenanceMode()
        } else {
            val detail = if (config.adminPinConfig.isConfigured) {
                getString(R.string.state_admin_pin_invalid)
            } else {
                getString(R.string.state_admin_pin_not_configured)
            }
            render(KioskUiState.AdminChallenge(detail))
        }
    }

    private fun enterMaintenanceMode() {
        mainHandler.removeCallbacks(maintenanceTimeoutRunnable)
        val stopResult = kioskController.stopLockTaskFromAdminFlow()
        logMaintenanceStart(stopResult)
        mainHandler.postDelayed(maintenanceTimeoutRunnable, config.adminSessionMillis)
        render(KioskUiState.Maintenance(maintenanceDetail(stopResult)))
    }

    private fun endMaintenanceMode() {
        mainHandler.removeCallbacks(maintenanceTimeoutRunnable)
        kioskLogger.info("Admin maintenance session ended")
        handleKioskStart(kioskController.startLockTaskIfAllowed())
    }

    private fun handlePrimaryAction() {
        when (currentUiState) {
            is KioskUiState.Maintenance -> endMaintenanceMode()
            else -> loadConfiguredSystem()
        }
    }

    private fun editableToCharArray(editable: CharSequence?): CharArray {
        if (editable.isNullOrEmpty()) return CharArray(0)
        return CharArray(editable.length) { index -> editable[index] }
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
        currentUiState = state
        binding.setupPanel.visibility = View.GONE
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
            is KioskUiState.Maintenance -> getString(R.string.state_maintenance)
        }
        binding.statusMessage.text = when (state) {
            is KioskUiState.Offline -> state.detail
            is KioskUiState.LoadError -> state.detail.ifBlank { getString(R.string.state_load_error_detail) }
            is KioskUiState.NotProvisioned -> state.detail
            is KioskUiState.AdminChallenge -> state.detail
            is KioskUiState.Maintenance -> state.detail
            else -> ""
        }
        binding.statusMessage.visibility = when (state) {
            is KioskUiState.Offline, is KioskUiState.LoadError, is KioskUiState.NotProvisioned, is KioskUiState.AdminChallenge, is KioskUiState.Maintenance -> View.VISIBLE
            else -> View.GONE
        }
        binding.retryButton.text = when (state) {
            is KioskUiState.Maintenance -> getString(R.string.action_return_to_system)
            else -> getString(R.string.action_retry)
        }
        binding.retryButton.visibility = when (state) {
            is KioskUiState.Offline, is KioskUiState.LoadError, KioskUiState.BlockedNavigation, is KioskUiState.Maintenance -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun maintenanceDetail(stopResult: KioskStopResult): String {
        val minutes = (config.adminSessionMillis / 60_000L).coerceAtLeast(1L)
        return when (stopResult) {
            KioskStopResult.Stopped -> getString(R.string.state_maintenance_detail_stopped, minutes)
            KioskStopResult.Failed -> getString(R.string.state_maintenance_detail_stop_failed, minutes)
        }
    }

    private fun logMaintenanceStart(stopResult: KioskStopResult) {
        when (stopResult) {
            KioskStopResult.Stopped -> kioskLogger.info("Admin maintenance session started with Lock Task stopped")
            KioskStopResult.Failed -> kioskLogger.warning("Admin maintenance session started but Lock Task stop failed")
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
