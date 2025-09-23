package com.aptavis.projecttrackerchallenge.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aptavis.projecttrackerchallenge.R
import com.aptavis.projecttrackerchallenge.data.db.entity.TaskEntity
import com.aptavis.projecttrackerchallenge.databinding.ItemProjectCardBinding
import com.aptavis.projecttrackerchallenge.databinding.ItemTaskRowBinding
import com.aptavis.projecttrackerchallenge.domain.model.ProjectComputed
import com.aptavis.projecttrackerchallenge.domain.model.Status
import com.aptavis.projecttrackerchallenge.ui.common.StatusUi

class ProjectExpandableAdapter(
    private val onEditProject: (ProjectComputed) -> Unit,
    private val onQuickAddTask: (projectId: Long) -> Unit,
    private val onExpandRequested: (projectId: Long, expanded: Boolean) -> Unit,
    private val onTaskClicked: (TaskEntity) -> Unit = {},
) : ListAdapter<ProjectComputed, ProjectExpandableAdapter.VH>(Diff) {

    private val expandedIds = linkedSetOf<Long>()
    private val tasksByProjectId = hashMapOf<Long, List<TaskEntity>>()

    fun submitTasks(projectId: Long, tasks: List<TaskEntity>) {
        tasksByProjectId[projectId] = tasks
        val idx = currentList.indexOfFirst { it.id == projectId }
        if (idx != -1) notifyItemChanged(idx, PAYLOAD_TASKS)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProjectCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(
            item = currentList[position],
            isExpanded = expandedIds.contains(currentList[position].id)
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_TASKS)) {
            holder.bindTasksOnly(
                projectId = currentList[position].id,
                tasks = tasksByProjectId[currentList[position].id].orEmpty(),
                isExpanded = expandedIds.contains(currentList[position].id)
            )
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    inner class VH(private val b: ItemProjectCardBinding) : RecyclerView.ViewHolder(b.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: ProjectComputed, isExpanded: Boolean) {
            // Title
            b.tvTitle.text = item.name

            // Status badge (pakai StatusUi)
            applyProjectStatus(item.status)

            // Progress
            val pct = item.progress
            b.progress.progress = pct
            b.tvPercent.text = "$pct%"

            b.root.setOnClickListener { onEditProject(item) }

            // Meta
            val soon = item.deadlineSoonCount
            val soonSuffix = if (soon > 0) "$soon due soon" else ""
            b.tvMeta.text = "${item.taskCount} task \n$soonSuffix"

            // Actions
            b.btnAddTask.setOnClickListener { onQuickAddTask(item.id) }
            b.btnEdit.setOnClickListener { onEditProject(item) }

            // Expand / collapse
            bindExpandIcon(b.btnExpand, isExpanded)
            b.btnExpand.setOnClickListener {
                val newState = !expandedIds.contains(item.id)
                toggleExpand(item.id, newState)
                onExpandRequested(item.id, newState)
            }

            // Tasks section
            bindTasksOnly(item.id, tasksByProjectId[item.id].orEmpty(), isExpanded)
        }

        fun bindTasksOnly(projectId: Long, tasks: List<TaskEntity>, isExpanded: Boolean) {
            b.sectionTasks.isVisible = isExpanded
            bindExpandIcon(b.btnExpand, isExpanded)
            if (!isExpanded) return

            b.sectionTasks.removeAllViews()
            if (tasks.isEmpty()) {
                val empty = LayoutInflater.from(b.root.context)
                    .inflate(R.layout.view_empty_tasks_hint, b.sectionTasks, false)
                b.sectionTasks.addView(empty)
                return
            }

            val inflater = LayoutInflater.from(b.root.context)
            tasks.forEach { t ->
                val row = ItemTaskRowBinding.inflate(inflater, b.sectionTasks, false)

                // Title & meta
                row.tvTaskName.text = t.name
                row.tvWeight.text = "Weight: ${t.weight}"
                row.tvDeadline.text = t.deadlineAt?.let { fmtDate(it) } ?: "-"

                // Status chip konsisten
                applyTaskStatus(row, t.status)

                // Click
                row.root.setOnClickListener { onTaskClicked(t) }

                b.sectionTasks.addView(row.root)
            }
        }

        private fun toggleExpand(id: Long, expand: Boolean) {
            if (expand) expandedIds.add(id) else expandedIds.remove(id)
            @Suppress("DEPRECATION")
            val pos = adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                notifyItemChanged(pos, PAYLOAD_TASKS)
            }
        }

        private fun bindExpandIcon(iv: ImageView, expanded: Boolean) {
            iv.setImageResource(if (expanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more)
        }

        private fun applyProjectStatus(status: Status) {
            val ctx = b.root.context
            val chip = StatusUi.chip(status)
            b.badgeStatus.setCardBackgroundColor(ContextCompat.getColor(ctx, chip.bg))
            b.tvStatus.text = chip.label
            b.tvStatus.setTextColor(ContextCompat.getColor(ctx, chip.fg))
            b.statusDot.setBackgroundResource(chip.dot)
        }

        private fun applyTaskStatus(row: ItemTaskRowBinding, status: Status) {
            val ctx = row.root.context
            val chip = StatusUi.chip(status)
            row.badgeTaskStatus.setCardBackgroundColor(ContextCompat.getColor(ctx, chip.bg))
            row.tvTaskStatus.text = chip.label
            row.tvTaskStatus.setTextColor(ContextCompat.getColor(ctx, chip.fg))
            row.statusDot.setBackgroundResource(chip.dot)
        }
    }

    private fun fmtDate(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(millis))
    }

    private object Diff : DiffUtil.ItemCallback<ProjectComputed>() {
        override fun areItemsTheSame(oldItem: ProjectComputed, newItem: ProjectComputed) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ProjectComputed, newItem: ProjectComputed) =
            oldItem == newItem
    }

    private companion object {
        const val PAYLOAD_TASKS = "payload_tasks"
    }
}
