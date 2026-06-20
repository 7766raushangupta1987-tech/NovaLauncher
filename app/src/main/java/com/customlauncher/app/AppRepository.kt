package com.customlauncher.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class AppRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
    private val HIDDEN_KEY = "hidden_packages"

    fun getAllApps(): List<AppInfo> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

        return resolveInfos.map { resolveInfo ->
            AppInfo(
                label = resolveInfo.loadLabel(pm).toString(),
                packageName = resolveInfo.activityInfo.packageName,
                activityName = resolveInfo.activityInfo.name,
                icon = resolveInfo.loadIcon(pm)
            )
        }.sortedBy { it.label.lowercase() }
    }

    fun getVisibleApps(): List<AppInfo> {
        val hidden = getHiddenPackages()
        return getAllApps().filter { it.packageName !in hidden }
    }

    fun getHiddenPackages(): Set<String> {
        return prefs.getStringSet(HIDDEN_KEY, emptySet()) ?: emptySet()
    }

    fun hideApp(packageName: String) {
        val current = getHiddenPackages().toMutableSet()
        current.add(packageName)
        prefs.edit().putStringSet(HIDDEN_KEY, current).apply()
    }

    fun unhideApp(packageName: String) {
        val current = getHiddenPackages().toMutableSet()
        current.remove(packageName)
        prefs.edit().putStringSet(HIDDEN_KEY, current).apply()
    }
}
