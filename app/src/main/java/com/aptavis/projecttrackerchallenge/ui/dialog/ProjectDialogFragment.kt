package com.aptavis.projecttrackerchallenge.ui.dialog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.aptavis.projecttrackerchallenge.R
import com.aptavis.projecttrackerchallenge.databinding.DialogProjectBinding
import com.aptavis.projecttrackerchallenge.ui.viewmodel.ProjectDialogViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable

@AndroidEntryPoint
class ProjectDialogFragment : DialogFragment(R.layout.dialog_project) {

    data class Args(
        val modeEdit: Boolean = false,
        val projectId: Long? = null,
        val initialName: String? = null
    ) : Serializable

    private val viewModel: ProjectDialogViewModel by viewModels()

    private var _binding: DialogProjectBinding? = null
    private val binding get() = _binding!!

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = DialogProjectBinding.bind(view)

        val args = (requireArguments().getSerializable("args") as? Args) ?: Args()

        // Prefill nama saja (status tidak ada)
        binding.tvTitle.text = if (args.modeEdit) "Edit Project" else "New Project"
        binding.btnDelete.visibility = if (args.modeEdit) View.VISIBLE else View.GONE
        binding.etProjectName.setText(args.initialName.orEmpty())
        viewModel.setInitial(args.initialName)

        binding.btnSave.setOnClickListener {
            val name = binding.etProjectName.text?.toString()?.trim().orEmpty()
            if (name.isBlank()) {
                binding.etProjectName.error = "Name required"
                return@setOnClickListener
            }
            viewModel.onNameChange(name)

            // Animasi keluar ke kanan (aksi save)
            dialog?.window?.setWindowAnimations(R.style.DialogAnim_Base)
            viewModel.save(isEdit = args.modeEdit, projectId = args.projectId)
            dismissAllowingStateLoss()
        }

        binding.btnDelete.setOnClickListener {
            if (args.modeEdit && args.projectId != null) {
                // Animasi keluar ke kiri (aksi delete)
                dialog?.window?.setWindowAnimations(R.style.DialogAnim_Base)
                viewModel.delete(args.projectId)
                dismissAllowingStateLoss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun new(args: Args) = ProjectDialogFragment().apply {
            arguments = bundleOf("args" to args)
        }
    }
}
