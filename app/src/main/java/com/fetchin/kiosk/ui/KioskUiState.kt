package com.fetchin.kiosk.ui

sealed interface KioskUiState {
    data object Initializing : KioskUiState
    data object Loading : KioskUiState
    data object WebContent : KioskUiState
    data object Offline : KioskUiState
    data object LoadError : KioskUiState
    data object BlockedNavigation : KioskUiState
    data object NotProvisioned : KioskUiState
    data object Maintenance : KioskUiState
}
