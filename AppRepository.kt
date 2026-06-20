package com.novahome.launcher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.novahome.launcher.model.AppInfo
import com.novahome.launcher.utils.HideAppsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppRepository {

    // Known game category packages / keywords to detect games
    private val GAME_KEYWORDS = listOf(
        "game", "games", "gaming", "play", "clash", "pubg", "bgmi", "freefire",
        "cod", "minecraft", "roblox", "genshin", "temple", "subway", "chess",
        "ludo", "cricket", "football", "racing", "battle", "arena", "royale",
        "candy", "angry", "fruit", "ninja", "zombie", "shooter"
    )

    suspend fun loadInstalledApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val hidden = HideAppsManager.getHiddenPackages(context)
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        @Suppress("DEPRECATION")
        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        resolved
            .map { ri ->
                val pkg = ri.activityInfo.packageName
                AppInfo(
                    label = ri.loadLabel(pm).toString(),
                    packageName = pkg,
                    icon = ri.loadIcon(pm),
                    isGame = isLikelyGame(pkg)
                )
            }
            .distinctBy { it.packageName }
            .filter { !hidden.contains(it.packageName) }
            .sortedBy { it.label.lowercase() }
    }

    suspend fun loadAllApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map { ri ->
                val pkg = ri.activityInfo.packageName
                AppInfo(
                    label = ri.loadLabel(pm).toString(),
                    packageName = pkg,
                    icon = ri.loadIcon(pm),
                    isGame = isLikelyGame(pkg)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun isLikelyGame(packageName: String): Boolean {
        val lower = packageName.lowercase()
        return GAME_KEYWORDS.any { lower.contains(it) }
    }
}
