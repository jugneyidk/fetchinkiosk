package com.fetchin.kiosk.kiosk

import android.app.admin.DevicePolicyManager
import android.content.ComponentName

class DeviceOwnerStatusProvider(
    private val devicePolicyManager: DevicePolicyManager,
    private val adminComponent: ComponentName
) {
    fun isDeviceOwner(): Boolean = devicePolicyManager.isDeviceOwnerApp(adminComponent.packageName)

    fun isLockTaskPermitted(packageName: String): Boolean = devicePolicyManager.isLockTaskPermitted(packageName)
}
