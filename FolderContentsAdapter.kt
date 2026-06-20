package com.novahome.launcher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.novahome.launcher.R
import com.novahome.launcher.model.AppInfo

class FolderContentsAdapter(
    private var apps: List<AppInfo>,
    private val onClick: (AppInfo) -> Unit,
    private val onLongClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<FolderContentsAdapter.VH>() {

    fun updateApps(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }

    override fun getItemCount() = apps.size

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app_icon, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val app = apps[pos]
        h.icon.setImageDrawable(app.icon)
        h.label.text = app.label
        h.itemView.setOnClickListener { onClick(app) }
        h.itemView.setOnLongClickListener { onLongClick(app); true }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.iconImage)
        val label: TextView = v.findViewById(R.id.iconLabel)
    }
}
