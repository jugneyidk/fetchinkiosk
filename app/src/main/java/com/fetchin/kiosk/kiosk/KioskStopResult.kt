package com.fetchin.kiosk.kiosk

sealed interface KioskStopResult {
    data object Stopped : KioskStopResult
    data object Failed : KioskStopResult
}
