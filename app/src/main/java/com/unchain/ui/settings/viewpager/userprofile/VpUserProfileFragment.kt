package com.unchain.ui.settings.viewpager.userprofile

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.app.DatePickerDialog
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import android.widget.EditText
import com.unchain.R
import com.unchain.data.preferences.preferences.UserPreferencesManager
import com.unchain.databinding.FragmentVpUserProfileBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class VpUserProfileFragment : Fragment() {
    private var _binding: FragmentVpUserProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VpUserProfileViewModel by viewModels {
        VpUserProfileModelFactory(
            UserPreferencesManager(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVpUserProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.updateButton.setOnClickListener {
            showUpdateInfoDialog()
        }

        viewModel.userPreferences.observe(viewLifecycleOwner) { userPreferences ->
            binding.tvFullName.text = userPreferences.displayName
            binding.tvUserEmail.text = userPreferences.email
        }

        viewModel.height.observe(viewLifecycleOwner) { height ->
            binding.tvHeightVp.text = height.toString()
        }
        viewModel.weight.observe(viewLifecycleOwner) { weight ->
            binding.tvWeightVp.text = weight.toString()
        }
        viewModel.age.observe(viewLifecycleOwner) { age ->
            binding.tvDOBVp.text = age.toString()
        }

        observeUpdateProfileState()

        return view
    }

    private fun showUpdateInfoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_info, null)
        val heightInput = dialogView.findViewById<EditText>(R.id.etHeight)
        val weightInput = dialogView.findViewById<EditText>(R.id.etWeight)
        val dobInput = dialogView.findViewById<EditText>(R.id.etDOB)

        // Set up date picker
        dobInput.setOnClickListener {
            showDatePicker(dobInput)
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(dialogView)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()

            getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setOnClickListener {
                    if (heightInput.text.isNullOrBlank() || weightInput.text.isNullOrBlank() || dobInput.text.isNullOrBlank()) {
                        Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    viewModel.updateUserInfo(
                        newHeight = heightInput.text.toString(),
                        newWeight = weightInput.text.toString(),
                        newAge = dobInput.text.toString()
                    )
                    dismiss()
                }
            }

            getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setOnClickListener { dismiss() }
            }
        }
    }

    private fun showDatePicker(dobInput: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dobInput.setText(dateFormat.format(calendar.time))
            },
            year,
            month,
            day
        ).show()
    }

    private fun observeUpdateProfileState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateProfileState.collect { state ->
                when (state) {
                    is UpdateProfileState.Loading -> {
                        // Show loading if needed
                    }
                    is UpdateProfileState.Success -> {
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    is UpdateProfileState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}