package com.aptavis.projecttrackerchallenge.ui.dialog

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.app.NotificationManagerCompat
import com.aptavis.projecttrackerchallenge.R
import com.aptavis.projecttrackerchallenge.databinding.DialogTaskBinding
import com.aptavis.projecttrackerchallenge.domain.model.Status
import com.aptavis.projecttrackerchallenge.ui.viewmodel.TaskDialogViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable

@AndroidEntryPoint
class TaskDialogFragment : DialogFragment(R.layout.dialog_task) {

    data class Args(
        val projectId: Long,
        val modeEdit: Boolean = false,
        val taskId: Long? = null,
        val initialName: String? = null,
        val initialStatus: Status = Status.Draft,
        val initialWeight: Int = 1,
        val initialDeadlineAt: Long? = null,
        val initialNotify: Boolean = false
    ) : Serializable

    private val viewModel: TaskDialogViewModel by viewModels()

    // ViewBinding
    private var _binding: DialogTaskBinding? = null
    private val binding get() = _binding!!

    private var selectedDeadlineAt: Long? = null
    private var changingSwitchProgrammatically = false

    // Permission launcher untuk Android 13+ (POST_NOTIFICATIONS)
    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                // Setelah izin diberikan, pastikan app notifications enabled juga
                if (!areAppNotificationsEnabled()) {
                    showEnableNotifSettings()
                    forceNotifySwitch(false)
                } else {
                    forceNotifySwitch(true)
                    viewModel.onNotifyChange(true)
                    toast("Notifications enabled ✅")
                }
            } else {
                // Ditolak → balik OFF
                forceNotifySwitch(false)
                toast("Permission denied. Notifications disabled.")
            }
        }

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
        _binding = DialogTaskBinding.bind(view)

        val args = (requireArguments().getSerializable("args") as? Args)
            ?: error("TaskDialog requires Args")

        setupStatusDropdown(binding.ddTaskStatus)

        binding.tvTitle.text = if (args.modeEdit) "Edit Task" else "New Task"
        binding.btnDelete.isVisible = args.modeEdit

        // Prefill UI dari args
        binding.etTaskName.setText(args.initialName.orEmpty())
        binding.ddTaskStatus.setText(args.initialStatus.name, false)
        binding.etWeight.setText(args.initialWeight.toString())
        selectedDeadlineAt = args.initialDeadlineAt
        binding.etDeadline.setText(formatDate(args.initialDeadlineAt))
        forceNotifySwitch(args.initialNotify) // set value tanpa trigger listener

        // Sinkronkan initial state ke VM
        viewModel.setInitial(
            name = args.initialName,
            status = args.initialStatus,
            weight = args.initialWeight,
            deadlineAt = args.initialDeadlineAt,
            notify = args.initialNotify
        )

        // Date picker
        val deadlineClicker = View.OnClickListener {
            val picker = com.google.android.material.datepicker.MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Select deadline")
                .setSelection(selectedDeadlineAt ?: System.currentTimeMillis())
                .build()
            picker.addOnPositiveButtonClickListener { millis ->
                selectedDeadlineAt = millis
                binding.etDeadline.setText(formatDate(millis))
                viewModel.onDeadlineChange(millis)
            }
            picker.show(childFragmentManager, "deadlinePicker")
        }
        binding.etDeadline.setOnClickListener(deadlineClicker)

        // Handle toggle notifications (minta izin kalau perlu)
        binding.swNotify.setOnCheckedChangeListener { _, checked ->
            if (changingSwitchProgrammatically) return@setOnCheckedChangeListener

            if (checked) {
                // 1) Android 13+: minta permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !hasPostNotificationsPermission()
                ) {
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return@setOnCheckedChangeListener
                }
                // 2) Cek apakah notifications for app dimatikan user
                if (!areAppNotificationsEnabled()) {
                    showEnableNotifSettings()
                    forceNotifySwitch(false)
                    return@setOnCheckedChangeListener
                }
                // OK
                viewModel.onNotifyChange(true)
            } else {
                viewModel.onNotifyChange(false)
            }
        }

        // Save
        binding.btnSave.setOnClickListener {
            val name = binding.etTaskName.text?.toString()?.trim().orEmpty()
            if (name.isBlank()) {
                binding.etTaskName.error = "Name required"
                return@setOnClickListener
            }
            val weight = binding.etWeight.text?.toString()?.toIntOrNull() ?: 1
            if (weight !in 1..10) {
                binding.etWeight.error = "Weight must be 1..10"
                return@setOnClickListener
            }
            val status = Status.valueOf(binding.ddTaskStatus.text.toString())
            val notify = binding.swNotify.isChecked
            val deadline = selectedDeadlineAt

            viewModel.onNameChange(name)
            viewModel.onWeightChange(weight)
            viewModel.onStatusChange(status)
            viewModel.onNotifyChange(notify)
            viewModel.onDeadlineChange(deadline)

            dialog?.window?.setWindowAnimations(R.style.DialogAnim_Base)
            viewModel.save(
                projectId = args.projectId,
                isEdit = args.modeEdit,
                taskId = args.taskId
            )
            dismissAllowingStateLoss()
        }

        // Delete
        binding.btnDelete.setOnClickListener {
            if (args.modeEdit && args.taskId != null) {
                dialog?.window?.setWindowAnimations(R.style.DialogAnim_Base)
                viewModel.delete(projectId = args.projectId, taskId = args.taskId)
                dismissAllowingStateLoss()
            }
        }
    }

    private fun setupStatusDropdown(dd: MaterialAutoCompleteTextView) {
        val items = Status.entries.map { it.name }
        dd.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items))
    }

    private fun formatDate(millis: Long?): String =
        millis?.let {
            java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(it))
        } ?: ""

    private fun hasPostNotificationsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PERMISSION_GRANTED
        } else true
    }

    private fun areAppNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
    }

    private fun showEnableNotifSettings() {
        // Arahkan user ke pengaturan notifikasi app
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            // beberapa device lama pakai keys lain; ini cukup untuk modern devices
            data = Uri.parse("package:${requireContext().packageName}")
        }
        toast("Enable notifications for this app to receive reminders.")
        startActivity(intent)
    }

    private fun forceNotifySwitch(checked: Boolean) {
        changingSwitchProgrammatically = true
        binding.swNotify.isChecked = checked
        changingSwitchProgrammatically = false
    }

    private fun toast(msg: String) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun new(args: Args) = TaskDialogFragment().apply {
            arguments = bundleOf("args" to args)
        }
    }
}
