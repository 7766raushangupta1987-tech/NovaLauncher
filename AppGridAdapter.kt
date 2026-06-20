package com.novahome.launcher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.novahome.launcher.R
import com.novahome.launcher.model.AppInfo

class AppGridAdapter(
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo, View) -> Unit
) : ListAdapter<AppInfo, AppGridAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app_icon, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val app = getItem(pos)
        h.icon.setImageDrawable(app.icon)
        h.label.text = app.label
        h.itemView.setOnClickListener { onAppClick(app) }
        h.itemView.setOnLongClickListener { onAppLongClick(app, it); true }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.iconImage)
        val label: TextView = v.findViewById(R.id.iconLabel)
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AppInfo>() {
            override fun areItemsTheSame(o: AppInfo, n: AppInfo) = o.packageName == n.packageName
            override fun areContentsTheSame(o: AppInfo, n: AppInfo) = o == n
        }
    }
}
