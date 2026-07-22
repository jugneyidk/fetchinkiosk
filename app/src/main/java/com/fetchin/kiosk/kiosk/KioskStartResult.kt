package com.fetchin.kiosk.kiosk

sealed interface KioskStartResult {
    data object Started : KioskStartResult
    data object NotPermitted : KioskStartResult
}
