package com.unchain.ui.settings.viewpager.settings

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.unchain.R
import com.unchain.auth.LoginActivity
import com.unchain.data.preferences.preferences.UserPreferencesManager
import com.unchain.databinding.FragmentVpSettingsBinding
import com.unchain.ui.home.PremiumFragment
import kotlinx.coroutines.launch

class VpSettingsFragment : Fragment() {

    private val viewModel: VpSettingsViewModel by viewModels()
    private var _binding: FragmentVpSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferencesManager = UserPreferencesManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVpSettingsBinding.inflate(inflater, container, false)
        setupPremiumButton(binding.root)
        return binding.root
    }

    private fun setupPremiumButton(view: View) {
        view.findViewById<View>(R.id.btnSettingsPremium)?.setOnClickListener {
            val premiumFragment = PremiumFragment()
            premiumFragment.show(parentFragmentManager, "premium_dialog")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLogoutButton()
        observeLogoutState()
    }

    private fun setupLogoutButton() {
        binding.logoutLayout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out? You'll need to sign in again to access your personalized content and settings.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Sign Out", null)
            .create()

        dialog.apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
            
            // Set button colors after dialog is shown
            getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                setOnClickListener {
                    dismiss()
                    viewModel.logout(userPreferencesManager)
                }
            }
            
            getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setOnClickListener {
                    dismiss()
                }
            }
        }
    }

    private fun observeLogoutState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.logoutState.collect { state ->
                when (state) {
                    is LogoutState.Loading -> {
                        // You could show a loading indicator here if needed
                    }
                    is LogoutState.Success -> {
                        // Navigate to login screen
                        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        requireActivity().finish()
                    }
                    is LogoutState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> { /* Initial state, do nothing */ }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}