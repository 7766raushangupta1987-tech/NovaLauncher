package com.novahome.launcher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novahome.launcher.R
import com.novahome.launcher.model.AppInfo
import com.novahome.launcher.model.HomeItem

/**
 * Feeds ViewPager2 directly with a plain RecyclerView.Adapter (no Fragments).
 * Each "page" is a small grid showing a slice of HomeItems (apps + folders).
 */
class HomePagesAdapter(
    private val pages: List<List<HomeItem>>,
    private val columns: Int,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo, View) -> Unit,
    private val onFolderClick: (HomeItem.Folder) -> Unit
) : RecyclerView.Adapter<HomePagesAdapter.PageVH>() {

    override fun getItemCount() = pages.size

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): PageVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_home_page, parent, false)
        return PageVH(v, columns, onAppClick, onAppLongClick, onFolderClick)
    }

    override fun onBindViewHolder(h: PageVH, pos: Int) = h.bind(pages[pos])

    class PageVH(
        itemView: View,
        cols: Int,
        onClick: (AppInfo) -> Unit,
        onLong: (AppInfo, View) -> Unit,
        onFolderClick: (HomeItem.Folder) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val grid: RecyclerView = itemView.findViewById(R.id.pageGrid)
        private val adapter = HomeGridAdapter(emptyList(), onClick, onLong, onFolderClick)
        init {
            grid.layoutManager = GridLayoutManager(itemView.context, cols)
            grid.adapter = adapter
            grid.setHasFixedSize(true)
            grid.itemAnimator = null
        }
        fun bind(items: List<HomeItem>) = adapter.submitItems(items)
    }
}
