package com.novahome.launcher.ui

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novahome.launcher.adapter.AppGridAdapter
import com.novahome.launcher.model.AppInfo

class AppDrawerController(
    private val drawerRoot: View,
    drawerGrid: RecyclerView,
    private val searchBox: EditText,
    private val tabAll: TextView,
    private val tabGames: TextView,
    columns: Int,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo, View) -> Unit
) {
    private var allApps: List<AppInfo> = emptyList()
    private var showGamesOnly = false
    private val adapter = AppGridAdapter(onAppClick, onAppLongClick)

    var isOpen = false
        private set

    init {
        drawerGrid.layoutManager = GridLayoutManager(drawerRoot.context, columns)
        drawerGrid.adapter = adapter
        drawerGrid.setHasFixedSize(true)
        drawerGrid.itemAnimator = null

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { applyFilter() }
        })

        tabAll.setOnClickListener {
            showGamesOnly = false
            tabAll.alpha = 1f
            tabGames.alpha = 0.5f
            applyFilter()
        }
        tabGames.setOnClickListener {
            showGamesOnly = true
            tabGames.alpha = 1f
            tabAll.alpha = 0.5f
            applyFilter()
        }
    }

    fun setApps(apps: List<AppInfo>) {
        allApps = apps
        applyFilter()
    }

    private fun applyFilter() {
        val query = searchBox.text?.toString().orEmpty()
        var filtered = if (showGamesOnly) allApps.filter { it.isGame } else allApps
        if (query.isNotBlank()) filtered = filtered.filter { it.label.contains(query, true) }
        adapter.submitList(filtered)
    }

    fun open() {
        if (isOpen) return
        isOpen = true
        drawerRoot.visibility = View.VISIBLE
        val startY = if (drawerRoot.height > 0) drawerRoot.height.toFloat() else 2400f
        drawerRoot.translationY = startY
        drawerRoot.animate().translationY(0f).setDuration(240).start()
    }

    fun close() {
        if (!isOpen) return
        isOpen = false
        searchBox.setText("")
        drawerRoot.animate()
            .translationY(drawerRoot.height.toFloat().coerceAtLeast(2400f))
            .setDuration(200)
            .withEndAction { drawerRoot.visibility = View.INVISIBLE }
            .start()
    }
}
