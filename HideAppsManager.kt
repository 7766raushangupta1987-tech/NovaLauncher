package com.novahome.launcher.utils

import android.content.Context

object HideAppsManager {
    private const val PREFS = "nova_hide_prefs"
    private const val KEY_HIDDEN = "hidden_packages"

    fun getHiddenPackages(ctx: Context): Set<String> =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()

    fun setHiddenPackages(ctx: Context, packages: Set<String>) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_HIDDEN, packages).apply()
    }
}
