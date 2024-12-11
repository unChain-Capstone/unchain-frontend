package com.unchain.ui.settings.viewpager.userprofile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.unchain.R
import com.unchain.data.preferences.preferences.UserPreferencesManager
import com.unchain.databinding.FragmentVpUserProfileBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class VpUserProfileFragment : Fragment() {
    private var _binding: FragmentVpUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: VpUserProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVpUserProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = ViewModelProvider(
            this,
            VpUserProfileViewModelFactory(UserPreferencesManager(requireContext()))
        )[VpUserProfileViewModel::class.java]

        binding.updateButton.setOnClickListener {
            showUpdateInfoDialog()
        }

        lifecycleScope.launch {
            viewModel.userPreferences.collect { userPreferences ->
                binding.tvFullName.text = userPreferences.displayName
                binding.tvUserEmail.text = userPreferences.email
                binding.tvDOBVp.text = userPreferences.dateOfBirth
                binding.tvHeightVp.text = "${userPreferences.height} cm"
                binding.tvWeightVp.text = "${userPreferences.weight} kg"
                binding.tvGenderVp.text = if (userPreferences.isMale) "Male" else "Female"
            }
        }

        viewModel.height.observe(viewLifecycleOwner) { height ->
            binding.tvHeightVp.text = "$height cm"
        }
        viewModel.weight.observe(viewLifecycleOwner) { weight ->
            binding.tvWeightVp.text = "$weight kg"
        }

        viewModel.dateOfBirth.observe(viewLifecycleOwner){ dateOfBirth ->
            binding.tvDOBVp.text = dateOfBirth
        }

        viewModel.gender.observe(viewLifecycleOwner) { isMale ->
            binding.tvGenderVp.text = if (isMale) "Male" else "Female"
        }

        observeUpdateProfileState()

        return view
    }

    private fun observeUpdateProfileState() {
        viewModel.updateProfileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UpdateProfileState.Loading -> {

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

    private fun showUpdateInfoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_info, null)
        val heightInput = dialogView.findViewById<EditText>(R.id.etHeight)
        val weightInput = dialogView.findViewById<EditText>(R.id.etWeight)
        val dobInput = dialogView.findViewById<EditText>(R.id.etDOB)
        val radioGroupGender = dialogView.findViewById<RadioGroup>(R.id.radioGroupGender)

        // Pre-fill current values
        viewModel.height.value?.let { heightInput.setText(it) }
        viewModel.weight.value?.let { weightInput.setText(it) }
        viewModel.gender.value?.let { isMale ->
            radioGroupGender.check(if (isMale) R.id.radioMale else R.id.radioFemale)
        }

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
                        dateOfBirth = dobInput.text.toString(),
                        isMale = radioGroupGender.checkedRadioButtonId == R.id.radioMale
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
                val formattedDate = String.format(
                    "%d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
                dobInput.setText(formattedDate)
            },
            year,
            month,
            day
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class VpUserProfileViewModelFactory(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VpUserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VpUserProfileViewModel(userPreferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}