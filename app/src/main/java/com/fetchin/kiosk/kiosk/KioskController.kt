package com.fetchin.kiosk.kiosk

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.fetchin.kiosk.admin.KioskDeviceAdminReceiver

class KioskController(private val activity: Activity) {
    private val devicePolicyManager = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(activity, KioskDeviceAdminReceiver::class.java)
    private val statusProvider = DeviceOwnerStatusProvider(devicePolicyManager, adminComponent)

    fun startLockTaskIfAllowed(): KioskStartResult {
        return if (statusProvider.isLockTaskPermitted(activity.packageName)) {
            activity.startLockTask()
            KioskStartResult.Started
        } else {
            KioskStartResult.NotPermitted
        }
    }

    fun stopLockTaskFromAdminFlow(): KioskStopResult {
        return runCatching {
            activity.stopLockTask()
            KioskStopResult.Stopped
        }.getOrElse {
            KioskStopResult.Failed
        }
    }

    fun isDeviceOwner(): Boolean = statusProvider.isDeviceOwner()
}
