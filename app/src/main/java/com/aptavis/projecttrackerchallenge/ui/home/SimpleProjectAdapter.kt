package com.aptavis.projecttrackerchallenge.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aptavis.projecttrackerchallenge.domain.model.ProjectComputed

class SimpleProjectAdapter :
    RecyclerView.Adapter<SimpleProjectAdapter.VH>() {

    private val data = mutableListOf<ProjectComputed>()

    fun submit(newData: List<ProjectComputed>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    class VH(val title: TextView, val subtitle: TextView) : RecyclerView.ViewHolder(
        LayoutInflater.from(title.context)
            .inflate(android.R.layout.simple_list_item_2, null, false)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        val title = itemView.findViewById<TextView>(android.R.id.text1)
        val subtitle = itemView.findViewById<TextView>(android.R.id.text2)
        return VH(title, subtitle)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        holder.title.text = item.name
        holder.subtitle.text = "${item.taskCount} task • ${item.progress}% • ${item.status}"
    }

    override fun getItemCount() = data.size
}
