# Nova Home Launcher v3.0

## What's new in v3
- **Glassy / transparent icons** — Apple-style frosted-glass look on Samsung-style rounded icon tiles
- **Smart Large Folders** — tap a mini-icon inside a closed folder to launch that app directly; tap the rest of the folder tile to open the full view
- **Standalone Gaming Hub** — now its own app in the drawer (own icon, own entry), no longer a popup. Includes a "Free up memory" shortcut.
- **Double-tap to lock** — double-tap empty home space to lock the phone (needs a one-time Device Admin permission — same mechanism Nova Launcher / Microsoft Launcher use for this exact feature)
- **Wallpaper-based accent color** — the Apps button picks up the dominant color from your current wallpaper automatically
- **Storage cleaner shortcut** — opens Android's own Storage settings page (the system's real cleanup tool, not a fake "1-tap clean")
- **Smoother app-launch transition** — fade transition instead of an abrupt cut
- All previous v2 features: clock, swipe pages, hotseat, app drawer search, Hide Apps, grid/dock size settings, long-press edit sheet (Wallpaper/Widgets/Themes/Settings)

## What's intentionally NOT included (and why)
- **No system Quick Settings panel replacement.** Android does not allow any app to intercept or replace the system status-bar swipe-down panel. This applies to every launcher, including Nova Launcher and Microsoft Launcher — not a limitation specific to this project.
- **No "1-tap clear all app caches."** Since Android 6.0, no normal app can read or clear another app's cache directly. The in-app "Storage" shortcut opens the real system Storage page instead of faking a result.
- **No "freeze all background apps."** Android's own Doze/App Standby already manages this. The Gaming Hub's "Free up memory" button does the one thing apps are actually allowed to do: trim cached, already-stopped background processes via the public API.

## Build instructions
Open in Android Studio, let Gradle sync, **Build → Build APK(s)**, install `app-debug.apk`, then set as default Home app in Settings → Apps → Default apps → Home app.

## Folders — how they work
- Long-press any app icon → "Add to folder" → pick an existing folder or create a new one
- Folders show as a tile with up to 4 mini-icons
- Tap a mini-icon to launch that app immediately, no need to open the folder
- Tap anywhere else on the tile to open the full folder view
- Long-press an app inside the open folder to remove it (folder auto-deletes once empty)

## Double-tap to lock — setup
First use (or via Home Settings → "Double-tap to lock") prompts a one-time system permission dialog, required by Android for any app locking the screen programmatically.
