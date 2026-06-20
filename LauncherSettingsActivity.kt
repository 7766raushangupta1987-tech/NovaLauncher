package com.novahome.launcher.ui

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.novahome.launcher.databinding.ActivityLauncherSettingsBinding
import com.novahome.launcher.utils.LauncherPrefs

class LauncherSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLauncherSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.settingsBack.setOnClickListener { finish() }

        // Grid columns: 3/4/5
        val colsMap = listOf(3, 4, 5)
        val curCols = LauncherPrefs.getGridColumns(this)
        binding.gridColumnsSeek.progress = colsMap.indexOf(curCols).coerceAtLeast(0)
        binding.gridColumnsLabel.text = "$curCols columns"
        binding.gridColumnsSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val cols = colsMap[progress]
                binding.gridColumnsLabel.text = "$cols columns"
                LauncherPrefs.setGridColumns(this@LauncherSettingsActivity, cols)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) { setResult(RESULT_OK) }
        })

        // Dock size: 3/4/5
        val dockMap = listOf(3, 4, 5)
        val curDock = LauncherPrefs.getDockSize(this)
        binding.dockSizeSeek.progress = dockMap.indexOf(curDock).coerceAtLeast(0)
        binding.dockSizeLabel.text = "$curDock icons"
        binding.dockSizeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = dockMap[progress]
                binding.dockSizeLabel.text = "$size icons"
                LauncherPrefs.setDockSize(this@LauncherSettingsActivity, size)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) { setResult(RESULT_OK) }
        })

        binding.goHideApps.setOnClickListener {
            startActivityForResult(Intent(this, HideAppsActivity::class.java), 200)
        }

        binding.goStorage.setOnClickListener {
            try {
                startActivity(Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
            } catch (e: Exception) {
                startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
            }
        }

        binding.goLockSetup.setOnClickListener {
            if (com.novahome.launcher.utils.LockScreenHelper.isAdminActive(this)) {
                android.widget.Toast.makeText(this, "Already enabled — double-tap any empty home space", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                com.novahome.launcher.utils.LockScreenHelper.requestAdmin(this)
            }
        }
    }
}
