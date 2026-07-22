package com.fetchin.kiosk.kiosk

sealed interface KioskStartResult {
    data class Started(val status: KioskProvisioningStatus) : KioskStartResult
    data class NotPermitted(val status: KioskProvisioningStatus) : KioskStartResult
    data class Failed(val status: KioskProvisioningStatus) : KioskStartResult
}
