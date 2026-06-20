package com.novahome.launcher.model

/**
 * A home screen grid slot is either a single app icon, or a "smart folder"
 * holding several apps. Folders render as one big tile showing up to 4
 * mini-icons; tapping a mini-icon launches that app directly without
 * opening the folder first (the "smart" part Samsung calls quick access).
 */
sealed class HomeItem {
    data class Single(val app: AppInfo) : HomeItem()
    data class Folder(val name: String, val apps: List<AppInfo>) : HomeItem()
}
