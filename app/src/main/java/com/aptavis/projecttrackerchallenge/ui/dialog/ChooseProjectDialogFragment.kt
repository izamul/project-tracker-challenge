package com.aptavis.projecttrackerchallenge.ui.dialog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aptavis.projecttrackerchallenge.R
import com.aptavis.projecttrackerchallenge.databinding.DialogChooseProjectBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable

@AndroidEntryPoint
class ChooseProjectDialogFragment : DialogFragment(R.layout.dialog_choose_project) {

    data class ProjectPick(val id: Long, val name: String) : Serializable

    companion object {
        private const val ARG_LIST = "list"
        const val RESULT_KEY = "choose_project_result"
        const val RESULT_ID = "project_id"

        fun new(list: List<ProjectPick>) = ChooseProjectDialogFragment().apply {
            arguments = bundleOf(ARG_LIST to ArrayList(list)) // Serializable
        }
    }

    private var _binding: DialogChooseProjectBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChooseProjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.App_Dialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setWindowAnimations(R.style.DialogAnim_Base)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = DialogChooseProjectBinding.bind(view)

        val list = (requireArguments().getSerializable(ARG_LIST) as? ArrayList<ProjectPick>) ?: arrayListOf()

        adapter = ChooseProjectAdapter { picked ->
            parentFragmentManager.setFragmentResult(
                RESULT_KEY,
                bundleOf(RESULT_ID to picked.id)
            )
            dismissAllowingStateLoss()
        }

        binding.rvProjects.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChooseProjectDialogFragment.adapter
            setHasFixedSize(true)
        }
        adapter.submitList(list)

        binding.btnClose.setOnClickListener { dismissAllowingStateLoss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
