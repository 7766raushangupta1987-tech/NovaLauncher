package com.novahome.launcher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.novahome.launcher.R
import com.novahome.launcher.model.AppInfo

class HideAppsAdapter(
    private val apps: List<AppInfo>,
    private val initialHidden: Set<String>
) : RecyclerView.Adapter<HideAppsAdapter.VH>() {

    val selectedPackages = initialHidden.toMutableSet()

    override fun getItemCount() = apps.size

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_hide_app, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val app = apps[pos]
        h.icon.setImageDrawable(app.icon)
        h.label.text = app.label
        h.check.isChecked = selectedPackages.contains(app.packageName)
        h.check.setOnCheckedChangeListener { _, checked ->
            if (checked) selectedPackages.add(app.packageName)
            else selectedPackages.remove(app.packageName)
        }
        h.itemView.setOnClickListener { h.check.isChecked = !h.check.isChecked }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.hideAppIcon)
        val label: TextView = v.findViewById(R.id.hideAppLabel)
        val check: CheckBox = v.findViewById(R.id.hideAppCheck)
    }
}
