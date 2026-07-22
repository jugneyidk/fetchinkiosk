package com.fetchin.kiosk.kiosk

data class KioskProvisioningStatus(
    val isDeviceOwner: Boolean,
    val isLockTaskPermitted: Boolean,
    val packageName: String
)
