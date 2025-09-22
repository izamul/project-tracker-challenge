package com.aptavis.projecttrackerchallenge.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aptavis.projecttrackerchallenge.R
import com.aptavis.projecttrackerchallenge.databinding.FragmentHomeBinding
import com.aptavis.projecttrackerchallenge.domain.model.ProjectComputed
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val vm: HomeViewModel by viewModels()

    // Adapter sederhana dulu; nanti kamu bisa ganti ke ProjectAdapter versi kartu
    private val adapter = SimpleProjectAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // RecyclerView
        binding.rvProjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProjects.adapter = adapter
        binding.rvProjects.setHasFixedSize(true)

        // Observe data dari Room via ViewModel
        vm.projects.observe(viewLifecycleOwner) { list ->
            adapter.submit(list)
        }

        // Klik header action (sementara dummy)
        binding.btnAddProject.setOnClickListener {
            // TODO: tampilkan BottomSheet Add Project
            // vm.addProject("New Project")
        }
        binding.btnAddTask.setOnClickListener {
            // TODO: pilih project dulu atau buka sheet Add Task contextual
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
