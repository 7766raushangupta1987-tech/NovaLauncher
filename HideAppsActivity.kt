package com.novahome.launcher.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.novahome.launcher.adapter.HideAppsAdapter
import com.novahome.launcher.data.AppRepository
import com.novahome.launcher.databinding.ActivityHideAppsBinding
import com.novahome.launcher.utils.HideAppsManager
import kotlinx.coroutines.launch

class HideAppsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHideAppsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHideAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { finish() }

        lifecycleScope.launch {
            val apps = AppRepository.loadAllApps(applicationContext)
            val hidden = HideAppsManager.getHiddenPackages(applicationContext)
            val adapter = HideAppsAdapter(apps, hidden)
            binding.hideAppsRecycler.layoutManager = LinearLayoutManager(this@HideAppsActivity)
            binding.hideAppsRecycler.adapter = adapter

            binding.saveHideBtn.setOnClickListener {
                HideAppsManager.setHiddenPackages(applicationContext, adapter.selectedPackages)
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
