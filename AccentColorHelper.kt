package com.novahome.launcher.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette

/**
 * Reads the current wallpaper and extracts its dominant color using
 * AndroidX Palette, so the launcher's accent (dock highlight, Apps
 * button, page dots) can visually match whatever wallpaper is set —
 * similar to Samsung's "Color palette" theming.
 */
object AccentColorHelper {

    fun extractAccentColor(context: Context, fallback: Int): Int {
        return try {
            val wm = WallpaperManager.getInstance(context)
            val drawable = wm.drawable as? BitmapDrawable ?: return fallback
            val bitmap: Bitmap = drawable.bitmap ?: return fallback
            val palette = Palette.from(bitmap).generate()
            palette.getVibrantColor(
                palette.getDominantColor(fallback)
            )
        } catch (e: SecurityException) {
            // READ_EXTERNAL_STORAGE not granted on some OEMs for wallpaper access — fall back gracefully
            fallback
        } catch (e: Exception) {
            fallback
        }
    }
}
