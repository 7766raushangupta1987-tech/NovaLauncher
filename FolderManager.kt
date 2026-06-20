package com.novahome.launcher.utils

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists "smart folders" as JSON in SharedPreferences:
 * { "folderId": { "name": "Games", "packages": ["com.x","com.y"] }, ... }
 *
 * Kept deliberately simple (no database) since folder counts are small
 * and this is read once at home-screen load time.
 */
object FolderManager {
    private const val PREFS = "nova_folders_prefs"
    private const val KEY_FOLDERS = "folders_json"

    data class FolderDef(val id: String, val name: String, val packages: MutableList<String>)

    fun getFolders(ctx: Context): List<FolderDef> {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_FOLDERS, null)
            ?: return emptyList()
        return try {
            val obj = JSONObject(raw)
            obj.keys().asSequence().map { id ->
                val f = obj.getJSONObject(id)
                val pkgsArr = f.getJSONArray("packages")
                val pkgs = MutableList(pkgsArr.length()) { i -> pkgsArr.getString(i) }
                FolderDef(id, f.getString("name"), pkgs)
            }.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveFolders(ctx: Context, folders: List<FolderDef>) {
        val obj = JSONObject()
        folders.forEach { f ->
            val fo = JSONObject()
            fo.put("name", f.name)
            fo.put("packages", JSONArray(f.packages))
            obj.put(f.id, fo)
        }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_FOLDERS, obj.toString()).apply()
    }

    fun createFolder(ctx: Context, name: String, firstPackage: String): FolderDef {
        val folders = getFolders(ctx).toMutableList()
        val id = "folder_${System.currentTimeMillis()}"
        val def = FolderDef(id, name, mutableListOf(firstPackage))
        folders.add(def)
        saveFolders(ctx, folders)
        return def
    }

    fun addToFolder(ctx: Context, folderId: String, packageName: String) {
        val folders = getFolders(ctx).toMutableList()
        folders.find { it.id == folderId }?.packages?.let {
            if (!it.contains(packageName)) it.add(packageName)
        }
        saveFolders(ctx, folders)
    }

    fun removeFromFolder(ctx: Context, folderId: String, packageName: String) {
        val folders = getFolders(ctx).toMutableList()
        folders.find { it.id == folderId }?.packages?.remove(packageName)
        // Drop empty folders automatically
        saveFolders(ctx, folders.filter { it.packages.isNotEmpty() })
    }

    fun packagesInAnyFolder(ctx: Context): Set<String> =
        getFolders(ctx).flatMap { it.packages }.toSet()
}
