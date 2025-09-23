package com.aptavis.projecttrackerchallenge.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aptavis.projecttrackerchallenge.R
import com.aptavis.projecttrackerchallenge.databinding.FragmentHomeBinding
import com.aptavis.projecttrackerchallenge.domain.model.ProjectComputed
import com.aptavis.projecttrackerchallenge.ui.dialog.ChooseProjectDialogFragment
import com.aptavis.projecttrackerchallenge.ui.dialog.ProjectDialogFragment
import com.aptavis.projecttrackerchallenge.ui.dialog.TaskDialogFragment
import com.aptavis.projecttrackerchallenge.ui.profile.ProfileFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val vm: HomeViewModel by viewModels()

    private lateinit var adapter: ProjectExpandableAdapter

    private var latestProjects: List<ProjectComputed> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        adapter = ProjectExpandableAdapter(
            onEditProject = { p: ProjectComputed -> openEditProject(p) },
            onQuickAddTask = { projectId: Long -> openAddTaskForProject(projectId) },
            onExpandRequested = { projectId: Long, expanded: Boolean ->
                if (expanded) {
                    vm.tasks(projectId).observe(viewLifecycleOwner) { tasks ->
                        adapter.submitTasks(projectId, tasks)
                    }
                }
            },
            onTaskClicked = { task ->
                TaskDialogFragment.new(
                    TaskDialogFragment.Args(
                        projectId = task.projectId,
                        modeEdit = true,
                        taskId = task.id,
                        initialName = task.name,
                        initialStatus = task.status,
                        initialWeight = task.weight,
                        initialDeadlineAt = task.deadlineAt,
                        initialNotify = task.notifyEnabled
                    )
                ).show(childFragmentManager, "TaskDialog")
            }
        )

        binding.rvProjects.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
            setHasFixedSize(true)
        }

        vm.projects.observe(viewLifecycleOwner) { list: List<ProjectComputed> ->
            latestProjects = list
            adapter.submitList(list)
        }

        binding.btnAddProject.setOnClickListener { openAddProject() }

        binding.btnAddProject.setOnClickListener { openAddProject() }

        childFragmentManager.setFragmentResultListener(
            ChooseProjectDialogFragment.RESULT_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val projectId = bundle.getLong(ChooseProjectDialogFragment.RESULT_ID)
            openAddTaskForProject(projectId)
        }

        binding.btnAddTask.setOnClickListener {

            if (latestProjects.isEmpty()) {
                snackbar("Bikin project dulu, baru tambah task")
            } else {
                val picks = latestProjects.map { p ->
                    ChooseProjectDialogFragment.ProjectPick(id = p.id, name = p.name)
                }
                ChooseProjectDialogFragment.new(picks)
                    .show(childFragmentManager, "ChooseProjectDialog")
            }
        }

        binding.clTop.setOnClickListener { openProfile() }
    }


    private fun openAddProject() {
        ProjectDialogFragment
            .new(ProjectDialogFragment.Args())
            .show(childFragmentManager, "ProjectDialog")
    }

    private fun openEditProject(p: ProjectComputed) {
        ProjectDialogFragment
            .new(
                ProjectDialogFragment.Args(
                    modeEdit = true,
                    projectId = p.id,
                    initialName = p.name
                )
            )
            .show(childFragmentManager, "ProjectDialog")
    }

    private fun openProfile() {
        findNavController().navigate(R.id.action_home_to_profile)

    }

    private fun openAddTaskForProject(projectId: Long) {
        TaskDialogFragment
            .new(TaskDialogFragment.Args(projectId = projectId))
            .show(childFragmentManager, "TaskDialog")
    }


    private fun snackbar(msg: String) {
        com.google.android.material.snackbar.Snackbar
            .make(requireView(), msg, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

