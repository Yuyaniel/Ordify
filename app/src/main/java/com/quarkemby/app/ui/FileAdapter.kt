package com.quarkemby.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quarkemby.app.R
import com.quarkemby.app.data.models.FileItem

/**
 * Identity/content diff for the file list: rows are keyed by fid, so a refresh
 * only rebinds what actually changed.
 */
private val FILE_DIFF = object : DiffUtil.ItemCallback<FileItem>() {
    override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean =
        oldItem.fid == newItem.fid

    override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean =
        oldItem == newItem
}

/**
 * Adapter for the drive file list. Simple two-mode interaction:
 * tap opens a folder, long-press opens the context menu.
 *
 * Backed by [ListAdapter], so a refresh / re-sort diffs the new list against the
 * old one instead of rebinding every row (no full-list flicker).
 */
class FileAdapter(
    private val onClick: (FileItem) -> Unit,
    private val onLongClick: (FileItem) -> Unit
) : ListAdapter<FileItem, FileAdapter.VH>(FILE_DIFF) {

    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return ""
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var v = bytes.toDouble(); var u = 0
        while (v >= 1024 && u < units.size - 1) { v /= 1024; u++ }
        return String.format("%.1f %s", v, units[u])
    }

    private fun iconRes(item: FileItem): Int = when {
        item.isFolder -> R.drawable.ic_folder_yellow
        item.isVideo -> R.drawable.ic_video
        item.isSubtitle -> R.drawable.ic_subtitle
        else -> R.drawable.ic_file
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_file, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)
        h.icon.setImageResource(iconRes(item))
        h.name.text = item.name
        h.meta.text = when {
            item.isFolder -> "文件夹"
            item.isSubtitle -> "字幕 · ${item.size}B"
            else -> formatSize(item.size)
        }
        h.root.setOnClickListener { onClick(item) }
        h.root.setOnLongClickListener { onLongClick(item); true }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val root: View = v
        val icon: ImageView = v.findViewById(R.id.file_icon)
        val name: TextView = v.findViewById(R.id.file_name)
        val meta: TextView = v.findViewById(R.id.file_meta)
    }
}
