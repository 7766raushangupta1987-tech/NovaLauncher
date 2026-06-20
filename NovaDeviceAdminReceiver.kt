package com.novahome.launcher.utils

import android.app.admin.DeviceAdminReceiver

/**
 * Required by Android for any app that wants permission to lock the
 * screen programmatically (double-tap-to-lock). The user must approve
 * this once in a system dialog — same as Nova Launcher / Microsoft
 * Launcher do for the same feature. No extra permissions beyond
 * "lock screen" are requested.
 */
class NovaDeviceAdminReceiver : DeviceAdminReceiver()
