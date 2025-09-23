package com.aptavis.projecttrackerchallenge.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aptavis.projecttrackerchallenge.R
import com.aptavis.projecttrackerchallenge.databinding.FragmentProfileBinding
import com.aptavis.projecttrackerchallenge.prefs.ThemePrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    @Inject lateinit var themePrefs: ThemePrefs

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)


        // --- Header actions ---
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        viewLifecycleOwner.lifecycleScope.launch {
            binding.switchDarkMode.isChecked = themePrefs.isDarkFlow.first()
        }

        // persist on toggle
        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            viewLifecycleOwner.lifecycleScope.launch {
                themePrefs.setDark(checked)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
