package com.novahome.launcher

import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.novahome.launcher.adapter.FolderContentsAdapter
import com.novahome.launcher.adapter.HomePagesAdapter
import com.novahome.launcher.data.AppRepository
import com.novahome.launcher.databinding.ActivityMainBinding
import com.novahome.launcher.databinding.DialogFolderContentsBinding
import com.novahome.launcher.gaming.GamingHubActivity
import com.novahome.launcher.model.AppInfo
import com.novahome.launcher.model.HomeItem
import com.novahome.launcher.ui.AppDrawerController
import com.novahome.launcher.ui.HideAppsActivity
import com.novahome.launcher.ui.LauncherSettingsActivity
import com.novahome.launcher.utils.AccentColorHelper
import com.novahome.launcher.utils.FolderManager
import com.novahome.launcher.utils.LauncherPrefs
import com.novahome.launcher.utils.LockScreenHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerController: AppDrawerController
    private var allApps: List<AppInfo> = emptyList()

    private val clockHandler = Handler(Looper.getMainLooper())
    private val clockTicker = object : Runnable {
        override fun run() {
            updateClock()
            clockHandler.postDelayed(this, 1000L)
        }
    }

    private var editSheetShown = false
    private var lastTapTime = 0L

    companion object {
        private const val APPS_PER_PAGE = 20
        private const val REQ_SETTINGS = 100
        private const val DOUBLE_TAP_MS = 300L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Full-screen wallpaper window
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyAccentColor()
        setupDrawerController()
        setupSwipeGesture()
        setupLongPressAndDoubleTap()
        setupEditSheet()
        setupBackPressed()

        loadApps()
        updateClock()
    }

    // ───────────────────────────────────────────────
    // Wallpaper-based accent color (Samsung "Color palette" style)
    // ───────────────────────────────────────────────
    private fun applyAccentColor() {
        val fallback = androidx.core.content.ContextCompat.getColor(this, R.color.onui_accent)
        val accent = try {
            AccentColorHelper.extractAccentColor(this, fallback)
        } catch (e: Exception) {
            fallback
        }
        binding.appsButton.background?.setTint(accent)
    }

    // ───────────────────────────────────────────────
    // Drawer setup
    // ───────────────────────────────────────────────
    private fun setupDrawerController() {
        val dv = binding.appDrawer
        drawerController = AppDrawerController(
            drawerRoot = dv.root,
            drawerGrid = dv.drawerGrid,
            searchBox = dv.searchBox,
            tabAll = dv.tabAll,
            tabGames = dv.tabGames,
            columns = LauncherPrefs.getGridColumns(this),
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app, anchor -> showAppMenu(app, anchor) }
        )
        binding.appsButton.setOnClickListener { drawerController.open() }
    }

    // ───────────────────────────────────────────────
    // Load apps + build home pages (apps + smart folders)
    // ───────────────────────────────────────────────
    private fun loadApps() {
        lifecycleScope.launch {
            val apps = AppRepository.loadInstalledApps(applicationContext)
            allApps = apps
            setupHomePages(apps)
            setupHotseatIcons(apps)
            drawerController.setApps(apps)
        }
    }

    private fun buildHomeItems(apps: List<AppInfo>): List<HomeItem> {
        val folders = FolderManager.getFolders(this)
        val byPkg = apps.associateBy { it.packageName }
        val inFolders = folders.flatMap { it.packages }.toSet()

        val items = mutableListOf<HomeItem>()
        // Folders appear first, in the order they were created
        folders.forEach { f ->
            val folderApps = f.packages.mapNotNull { byPkg[it] }
            if (folderApps.isNotEmpty()) {
                items.add(HomeItem.Folder(f.name, folderApps))
            }
        }
        // Remaining apps (not in any folder) fill the rest of the grid
        apps.filter { it.packageName !in inFolders }.forEach { items.add(HomeItem.Single(it)) }
        return items
    }

    // ───────────────────────────────────────────────
    // Home pages (ViewPager2)
    // ───────────────────────────────────────────────
    private fun setupHomePages(apps: List<AppInfo>) {
        val cols = LauncherPrefs.getGridColumns(this)
        val homeItems = buildHomeItems(apps)
        val pages = homeItems.chunked(APPS_PER_PAGE)

        binding.homePager.adapter = HomePagesAdapter(
            pages = pages,
            columns = cols,
            onAppClick = { launchAppWithTransition(it) },
            onAppLongClick = { app, anchor -> showAppMenu(app, anchor) },
            onFolderClick = { folder -> showFolderDialog(folder) }
        )
        binding.homePager.offscreenPageLimit = 1
        setupPageIndicator(pages.size)
        binding.homePager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(pos: Int) = updatePageIndicator(pos)
        })
    }

    private fun refreshHomePages() {
        setupHomePages(allApps)
    }

    private fun setupPageIndicator(count: Int) {
        binding.pageIndicator.removeAllViews()
        val dp6 = (6 * resources.displayMetrics.density).toInt()
        val dp4 = (4 * resources.displayMetrics.density).toInt()
        repeat(count) { i ->
            val dot = View(this)
            val lp = android.widget.LinearLayout.LayoutParams(dp6, dp6)
            lp.marginStart = dp4; lp.marginEnd = dp4
            dot.layoutParams = lp
            dot.setBackgroundResource(if (i == 0) R.drawable.dot_active else R.drawable.dot_inactive)
            binding.pageIndicator.addView(dot)
        }
    }

    private fun updatePageIndicator(sel: Int) {
        for (i in 0 until binding.pageIndicator.childCount) {
            binding.pageIndicator.getChildAt(i)
                .setBackgroundResource(if (i == sel) R.drawable.dot_active else R.drawable.dot_inactive)
        }
    }

    // ───────────────────────────────────────────────
    // Smart Folders: expanded view + create/add/remove
    // ───────────────────────────────────────────────
    private fun showFolderDialog(folder: HomeItem.Folder) {
        val dialogBinding = DialogFolderContentsBinding.inflate(layoutInflater)
        dialogBinding.folderDialogTitle.text = folder.name
        dialogBinding.folderDialogGrid.layoutManager = GridLayoutManager(this, 4)

        val folderDef = FolderManager.getFolders(this).find { it.name == folder.name }
        val adapter = FolderContentsAdapter(
            apps = folder.apps,
            onClick = { app -> launchAppWithTransition(app) },
            onLongClick = { app ->
                if (folderDef != null) {
                    FolderManager.removeFromFolder(this, folderDef.id, app.packageName)
                    refreshHomePages()
                    Toast.makeText(this, "Removed from folder", Toast.LENGTH_SHORT).show()
                }
            }
        )
        dialogBinding.folderDialogGrid.adapter = adapter

        AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_NoActionBar)
            .setView(dialogBinding.root)
            .show()
    }

    private fun showAddToFolderDialog(app: AppInfo) {
        val folders = FolderManager.getFolders(this)
        val options = folders.map { it.name }.toMutableList()
        options.add("+ New folder")

        AlertDialog.Builder(this)
            .setTitle("Add to folder")
            .setItems(options.toTypedArray()) { _, which ->
                if (which == folders.size) {
                    promptNewFolderName(app)
                } else {
                    FolderManager.addToFolder(this, folders[which].id, app.packageName)
                    refreshHomePages()
                    Toast.makeText(this, "Added to ${folders[which].name}", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun promptNewFolderName(app: AppInfo) {
        val input = EditText(this).apply { hint = "Folder name" }
        AlertDialog.Builder(this)
            .setTitle("New folder")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().ifBlank { "Folder" }
                FolderManager.createFolder(this, name, app.packageName)
                refreshHomePages()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ───────────────────────────────────────────────
    // Hotseat dock
    // ───────────────────────────────────────────────
    private fun setupHotseatIcons(apps: List<AppInfo>) {
        val dockSize = LauncherPrefs.getDockSize(this)
        val slots = listOf(
            binding.hotseatSlot1,
            binding.hotseatSlot2,
            binding.hotseatSlot3,
            binding.hotseatSlot4
        )
        binding.hotseatSlot4.visibility = if (dockSize >= 4) View.VISIBLE else View.GONE

        val picks = apps.take(dockSize)
        slots.forEachIndexed { i, slot ->
            val app = picks.getOrNull(i)
            if (app != null) {
                slot.setImageDrawable(app.icon)
                slot.setOnClickListener { launchAppWithTransition(app) }
                slot.setOnLongClickListener { showAppMenu(app, slot); true }
            } else {
                slot.setImageDrawable(null)
                slot.setOnClickListener(null)
            }
        }
    }

    // ───────────────────────────────────────────────
    // Swipe-up gesture to open drawer
    // ───────────────────────────────────────────────
    private fun setupSwipeGesture() {
        val detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (e1 != null && e1.y - e2.y > 100 && vy < -300) {
                    if (!drawerController.isOpen) drawerController.open()
                    return true
                }
                return false
            }
        })

        binding.swipeUpCatcher.setOnTouchListener { v, event ->
            val consumed = detector.onTouchEvent(event)
            if (!consumed) v.performClick()
            consumed
        }
        binding.homePager.getChildAt(0)?.setOnTouchListener { _, event ->
            detector.onTouchEvent(event)
            false
        }
    }

    // ───────────────────────────────────────────────
    // Long-press home → edit sheet. Double-tap empty area → lock screen.
    // ───────────────────────────────────────────────
    private fun setupLongPressAndDoubleTap() {
        val detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                showEditSheet()
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                handleDoubleTapLock()
                return true
            }
        })

        binding.homePager.setOnTouchListener { _, event ->
            if (!drawerController.isOpen) detector.onTouchEvent(event)
            false
        }
    }

    private fun handleDoubleTapLock() {
        if (LockScreenHelper.isAdminActive(this)) {
            LockScreenHelper.lockNow(this)
        } else {
            AlertDialog.Builder(this)
                .setTitle("Enable double-tap to lock?")
                .setMessage("To lock your phone with a double-tap, Nova Home needs the Device Admin permission for screen lock — the same one Samsung's own launcher uses for this feature.")
                .setPositiveButton("Enable") { _, _ -> LockScreenHelper.requestAdmin(this) }
                .setNegativeButton("Not now", null)
                .show()
        }
    }

    // ───────────────────────────────────────────────
    // Home edit sheet (long-press)
    // ───────────────────────────────────────────────
    private fun setupEditSheet() {
        val sheet = binding.homeEditSheet

        sheet.btnWallpaper.setOnClickListener {
            dismissEditSheet()
            openWallpaperPicker()
        }
        sheet.btnWidgets.setOnClickListener {
            dismissEditSheet()
            openWidgetPicker()
        }
        sheet.btnThemes.setOnClickListener {
            dismissEditSheet()
            openThemesStore()
        }
        sheet.btnHomeSettings.setOnClickListener {
            dismissEditSheet()
            startActivityForResult(Intent(this, LauncherSettingsActivity::class.java), REQ_SETTINGS)
        }

        binding.dimOverlay.setOnClickListener {
            dismissEditSheet()
        }
    }

    private fun showEditSheet() {
        if (editSheetShown || drawerController.isOpen) return
        editSheetShown = true
        binding.dimOverlay.visibility = View.VISIBLE
        binding.dimOverlay.alpha = 0f
        binding.dimOverlay.animate().alpha(1f).setDuration(200).start()

        val sheet = binding.homeEditSheet.root
        sheet.visibility = View.VISIBLE
        sheet.translationY = sheet.height.toFloat().coerceAtLeast(800f)
        sheet.animate().translationY(0f).setDuration(260).start()
    }

    private fun dismissEditSheet() {
        if (!editSheetShown) return
        editSheetShown = false
        binding.dimOverlay.animate().alpha(0f).setDuration(180)
            .withEndAction { binding.dimOverlay.visibility = View.GONE }.start()
        val sheet = binding.homeEditSheet.root
        sheet.animate().translationY(1200f).setDuration(220)
            .withEndAction { sheet.visibility = View.GONE }.start()
    }

    // ───────────────────────────────────────────────
    // App menu (long-press on icon)
    // ───────────────────────────────────────────────
    private fun showAppMenu(app: AppInfo, anchor: View) {
        val popup = android.widget.PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, getString(R.string.app_info))
        popup.menu.add(0, 2, 0, "Add to folder")
        popup.menu.add(0, 3, 0, getString(R.string.gaming_hub))
        popup.menu.add(0, 4, 0, "Uninstall")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> openAppInfo(app.packageName)
                2 -> showAddToFolderDialog(app)
                3 -> startActivity(Intent(this, GamingHubActivity::class.java))
                4 -> openUninstall(app.packageName)
            }
            true
        }
        popup.show()
    }

    private fun openAppInfo(pkg: String) {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .apply { data = Uri.fromParts("package", pkg, null) })
    }

    private fun openUninstall(pkg: String) {
        startActivity(Intent(Intent.ACTION_DELETE)
            .apply { data = Uri.fromParts("package", pkg, null) })
    }

    // ───────────────────────────────────────────────
    // Wallpaper / Widgets / Themes
    // ───────────────────────────────────────────────
    private fun openWallpaperPicker() {
        try {
            val intent = Intent(Intent.ACTION_SET_WALLPAPER)
            startActivity(Intent.createChooser(intent, getString(R.string.wallpaper)))
        } catch (e: Exception) {
            Toast.makeText(this, "Wallpaper picker not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWidgetPicker() {
        try {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Widget picker not available on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openThemesStore() {
        val samsungPkg = "com.samsung.android.themestore"
        val fallbackUrl = "https://play.google.com/store/search?q=launcher+theme&c=apps"
        val intent = packageManager.getLaunchIntentForPackage(samsungPkg)
            ?: Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
        startActivity(intent)
    }

    // ───────────────────────────────────────────────
    // Launch app — with a subtle scale+fade transition so switching
    // apps feels like one continuous motion (One UI 7 style) instead
    // of an abrupt cut.
    // ───────────────────────────────────────────────
    private fun launchAppWithTransition(app: AppInfo) {
        val intent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (intent == null) {
            Toast.makeText(this, "Can't open ${app.label}", Toast.LENGTH_SHORT).show()
            return
        }
        val options = android.app.ActivityOptions.makeCustomAnimation(
            this, android.R.anim.fade_in, android.R.anim.fade_out
        )
        startActivity(intent, options.toBundle())
    }

    private fun launchApp(app: AppInfo) = launchAppWithTransition(app)

    // ───────────────────────────────────────────────
    // Clock
    // ───────────────────────────────────────────────
    private fun updateClock() {
        val now = Date()
        binding.clockText.text = SimpleDateFormat("h:mm", Locale.getDefault()).format(now)
        binding.dateText.text = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(now)
    }

    // ───────────────────────────────────────────────
    // Back press
    // ───────────────────────────────────────────────
    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    editSheetShown -> dismissEditSheet()
                    drawerController.isOpen -> drawerController.close()
                }
            }
        })
    }

    // ───────────────────────────────────────────────
    // Activity results (settings/admin changed → reload)
    // ───────────────────────────────────────────────
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_SETTINGS) {
            loadApps()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (editSheetShown) dismissEditSheet()
        if (drawerController.isOpen) drawerController.close()
        binding.homePager.setCurrentItem(0, true)
    }

    override fun onResume() {
        super.onResume()
        clockHandler.post(clockTicker)
        // Folders/hidden-apps may have changed elsewhere — cheap to refresh
        if (allApps.isNotEmpty()) refreshHomePages()
    }

    override fun onPause() {
        super.onPause()
        // Stop the clock ticker the instant we're not visible — this is
        // the main thing keeping background battery/CPU use at zero.
        clockHandler.removeCallbacksAndMessages(null)
    }
}
