package com.novahome.launcher.gaming

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.novahome.launcher.adapter.AppGridAdapter
import com.novahome.launcher.data.AppRepository
import com.novahome.launcher.databinding.ActivityGamingHubBinding
import com.novahome.launcher.model.AppInfo
import kotlinx.coroutines.launch

class GamingHubActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGamingHubBinding
    private val statsHandler = Handler(Looper.getMainLooper())
    private var gameModeOn = false

    private val statsUpdater = object : Runnable {
        override fun run() {
            updateStats()
            statsHandler.postDelayed(this, 2000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamingHubBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.gamingBack.setOnClickListener { finish() }

        binding.gameModeSwitch.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
            gameModeOn = checked
            applyGameMode(checked)
        }

        binding.freeMemoryButton.setOnClickListener { freeUpMemory() }

        binding.gamesGrid.layoutManager = GridLayoutManager(this, 4)
        binding.gamesGrid.itemAnimator = null

        loadGames()
    }

    private fun loadGames() {
        lifecycleScope.launch {
            val all = AppRepository.loadAllApps(applicationContext)
            val games = all.filter { it.isGame }

            val adapter = AppGridAdapter(
                onAppClick = { launchApp(it) },
                onAppLongClick = { _, _ -> }
            )
            binding.gamesGrid.adapter = adapter

            if (games.isEmpty()) {
                binding.noGamesText.visibility = android.view.View.VISIBLE
                binding.gamesGrid.visibility = android.view.View.GONE
            } else {
                binding.noGamesText.visibility = android.view.View.GONE
                binding.gamesGrid.visibility = android.view.View.VISIBLE
                adapter.submitList(games)
            }
        }
    }

    private fun launchApp(app: AppInfo) {
        packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
    }

    private fun freeUpMemory() {
        // Android only lets an app trim its OWN memory footprint and ask the
        // system to reclaim cached (already-stopped) background app processes
        // via the public killBackgroundProcesses-equivalent API. There is no
        // API for a normal app to forcibly close OTHER running apps — any
        // app claiming to "boost RAM by closing all apps" is either doing
        // this same safe trim, or lying. This does the real, safe thing.
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        try {
            am.runningAppProcesses?.forEach { proc ->
                if (proc.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    proc.pkgList.forEach { pkg ->
                        if (pkg != packageName) am.killBackgroundProcesses(pkg)
                    }
                }
            }
            android.widget.Toast.makeText(this, "Background apps trimmed", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Memory trim not available on this device", android.widget.Toast.LENGTH_SHORT).show()
        }
        updateStats()
    }

    private fun applyGameMode(on: Boolean) {
        // Game Mode: when ON, raise display performance flags
        // (actual GPU/CPU boosting requires system-level API; this sets the window hint)
        window.attributes = window.attributes.also { attr ->
            if (on) {
                attr.preferredDisplayModeId = 0 // request highest refresh rate
            }
        }
    }

    private fun updateStats() {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val usedMb = (mi.totalMem - mi.availMem) / (1024 * 1024)
        val totalMb = mi.totalMem / (1024 * 1024)
        binding.statRam.text = "RAM: ${usedMb}/${totalMb}MB"
        binding.statFps.text = if (gameModeOn) "Game Mode: ON" else "Game Mode: OFF"
    }

    override fun onResume() {
        super.onResume()
        statsHandler.post(statsUpdater)
    }

    override fun onPause() {
        super.onPause()
        statsHandler.removeCallbacks(statsUpdater)
    }
}
