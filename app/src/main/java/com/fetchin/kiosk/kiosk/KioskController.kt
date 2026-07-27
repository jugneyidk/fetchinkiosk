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
        val status = ensureLockTaskPackageAllowed() ?: return KioskStartResult.Failed(provisioningStatus())
        return if (status.isLockTaskPermitted) {
            runCatching {
                activity.startLockTask()
                KioskStartResult.Started(status)
            }.getOrElse {
                KioskStartResult.Failed(status)
            }
        } else {
            KioskStartResult.NotPermitted(status)
        }
    }

    private fun ensureLockTaskPackageAllowed(): KioskProvisioningStatus? {
        val status = provisioningStatus()
        if (!status.isDeviceOwner || status.isLockTaskPermitted) return status
        return runCatching {
            devicePolicyManager.setLockTaskPackages(adminComponent, arrayOf(activity.packageName))
            provisioningStatus()
        }.getOrElse {
            null
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

    fun provisioningStatus(): KioskProvisioningStatus = KioskProvisioningStatus(
        isDeviceOwner = statusProvider.isDeviceOwner(),
        isLockTaskPermitted = statusProvider.isLockTaskPermitted(activity.packageName),
        packageName = activity.packageName
    )
}
