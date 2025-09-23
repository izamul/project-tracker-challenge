package com.aptavis.projecttrackerchallenge.ui.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aptavis.projecttrackerchallenge.databinding.ItemChooseProjectRowBinding
import com.aptavis.projecttrackerchallenge.ui.dialog.ChooseProjectDialogFragment.ProjectPick

class ChooseProjectAdapter(
    private val onPick: (ProjectPick) -> Unit
) : ListAdapter<ProjectPick, ChooseProjectAdapter.VH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemChooseProjectRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemChooseProjectRowBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ProjectPick) {
            b.tvName.text = item.name
            b.root.setOnClickListener { onPick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<ProjectPick>() {
        override fun areItemsTheSame(oldItem: ProjectPick, newItem: ProjectPick) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ProjectPick, newItem: ProjectPick) = oldItem == newItem
    }
}
