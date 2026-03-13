package com.simats.ocularsurfaceai.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.api.HistoryItem

class RecentAnalysisAdapter(
    private val onClick: (HistoryItem) -> Unit = {}
) : RecyclerView.Adapter<RecentAnalysisAdapter.VH>() {

    private val items = mutableListOf<HistoryItem>()

    fun submitList(newItems: List<HistoryItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_analysis, parent, false)
        return VH(v, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class VH(itemView: View, private val onClick: (HistoryItem) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val img = itemView.findViewById<ImageView>(R.id.imgEye)
        private val tvDisease = itemView.findViewById<TextView>(R.id.tvDisease)
        private val tvMeta = itemView.findViewById<TextView>(R.id.tvMeta)
        private val tvDate = itemView.findViewById<TextView>(R.id.tvDate)

        fun bind(item: HistoryItem) {
            tvDisease.text = item.disease
            tvMeta.text = "${item.severity} • ${item.confidence}%"
            tvDate.text = "${item.date} • ${item.time}"

            if (item.image_url.isNotBlank()) {
                Glide.with(itemView)
                    .load(item.image_url)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(img)
            } else {
                img.setImageDrawable(null)
            }

            itemView.setOnClickListener { onClick(item) }
        }
    }
}
