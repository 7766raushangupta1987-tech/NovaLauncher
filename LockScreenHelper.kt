package com.novahome.launcher.utils

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object LockScreenHelper {

    fun getAdminComponent(ctx: Context): ComponentName =
        ComponentName(ctx, NovaDeviceAdminReceiver::class.java)

    fun isAdminActive(ctx: Context): Boolean {
        val dpm = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isAdminActive(getAdminComponent(ctx))
    }

    fun requestAdmin(ctx: Context) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getAdminComponent(ctx))
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Needed only so double-tap on the home screen can lock your phone, just like Samsung's own launcher."
            )
        }
        ctx.startActivity(intent)
    }

    fun lockNow(ctx: Context): Boolean {
        if (!isAdminActive(ctx)) return false
        val dpm = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        dpm.lockNow()
        return true
    }
}
