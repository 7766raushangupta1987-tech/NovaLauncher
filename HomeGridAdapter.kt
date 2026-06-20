package com.novahome.launcher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.novahome.launcher.R
import com.novahome.launcher.model.AppInfo
import com.novahome.launcher.model.HomeItem

/**
 * Renders both plain app icons and "smart folders" in the same grid.
 * Folders show up to 4 mini-icons; tapping a mini-icon launches that
 * app directly (no need to open the folder first) — tapping the rest
 * of the folder tile opens the full folder view.
 */
class HomeGridAdapter(
    private var items: List<HomeItem>,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo, View) -> Unit,
    private val onFolderClick: (HomeItem.Folder) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_APP = 0
        private const val TYPE_FOLDER = 1
    }

    fun submitItems(newItems: List<HomeItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) =
        if (items[position] is HomeItem.Folder) TYPE_FOLDER else TYPE_APP

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_FOLDER) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_folder_tile, parent, false)
            FolderVH(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app_icon, parent, false)
            AppVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeItem.Single -> (holder as AppVH).bind(item.app, onAppClick, onAppLongClick)
            is HomeItem.Folder -> (holder as FolderVH).bind(item, onAppClick, onFolderClick)
        }
    }

    class AppVH(v: View) : RecyclerView.ViewHolder(v) {
        private val icon: ImageView = v.findViewById(R.id.iconImage)
        private val label: TextView = v.findViewById(R.id.iconLabel)
        fun bind(app: AppInfo, onClick: (AppInfo) -> Unit, onLong: (AppInfo, View) -> Unit) {
            icon.setImageDrawable(app.icon)
            label.text = app.label
            itemView.setOnClickListener { onClick(app) }
            itemView.setOnLongClickListener { onLong(app, it); true }
        }
    }

    class FolderVH(v: View) : RecyclerView.ViewHolder(v) {
        private val minis = listOf<ImageView>(
            v.findViewById(R.id.folderMini1),
            v.findViewById(R.id.folderMini2),
            v.findViewById(R.id.folderMini3),
            v.findViewById(R.id.folderMini4)
        )
        private val label: TextView = v.findViewById(R.id.folderLabel)

        fun bind(folder: HomeItem.Folder, onAppClick: (AppInfo) -> Unit, onFolderClick: (HomeItem.Folder) -> Unit) {
            label.text = folder.name
            minis.forEachIndexed { i, iv ->
                val app = folder.apps.getOrNull(i)
                if (app != null) {
                    iv.setImageDrawable(app.icon)
                    iv.visibility = View.VISIBLE
                    // Smart tap: launch this specific app directly from the closed folder
                    iv.setOnClickListener { onAppClick(app) }
                } else {
                    iv.setImageDrawable(null)
                    iv.visibility = View.INVISIBLE
                    iv.setOnClickListener(null)
                }
            }
            // Tapping the label/background (not a mini-icon) opens the full folder
            itemView.setOnClickListener { onFolderClick(folder) }
        }
    }
}
