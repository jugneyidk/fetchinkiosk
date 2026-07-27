package com.fetchin.kiosk.ui

sealed interface KioskUiState {
    data object Initializing : KioskUiState
    data object Loading : KioskUiState
    data object WebContent : KioskUiState
    data class Offline(val detail: String) : KioskUiState
    data class LoadError(val detail: String) : KioskUiState
    data object BlockedNavigation : KioskUiState
    data class NotProvisioned(val detail: String) : KioskUiState
    data class AdminChallenge(val detail: String) : KioskUiState
    data class Maintenance(val detail: String) : KioskUiState
}
