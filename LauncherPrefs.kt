package com.novahome.launcher.utils

import android.content.Context

object LauncherPrefs {
    private const val PREFS = "nova_launcher_prefs"

    fun getGridColumns(ctx: Context): Int =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("grid_columns", 4)

    fun setGridColumns(ctx: Context, cols: Int) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt("grid_columns", cols).apply()

    fun getDockSize(ctx: Context): Int =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("dock_size", 3)

    fun setDockSize(ctx: Context, size: Int) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt("dock_size", size).apply()
}
