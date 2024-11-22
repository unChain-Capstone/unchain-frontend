package com.unchain.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.unchain.R
import com.unchain.activities.OnboardingActivity
import com.unchain.auth.LoginActivity
import com.unchain.data.preferences.model.UserPreferences
import com.unchain.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.userPreferences.observe(viewLifecycleOwner) { userPreferences: UserPreferences ->
            binding.apply {
                tvuserName.text = userPreferences.displayName
                tvuserEmail.text = userPreferences.email

                // Load profile image using Glide
                if (userPreferences.photoUrl.isNotEmpty()) {
                    Glide.with(this@SettingsFragment)
                        .load(userPreferences.photoUrl)
                        .apply(RequestOptions().transform(RoundedCorners(10)))
                        .into(userProfile)
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Edit Profile Button
        binding.btnEditProfile.setOnClickListener {
            // TODO: Implement edit profile functionality
        }

        // Logout
        binding.root.findViewById<View>(R.id.logout_layout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), OnboardingActivity::class.java))
            requireActivity().finish()
        }

        // Support
        binding.root.findViewById<View>(R.id.support_layout).setOnClickListener {
            // TODO: Implement support functionality
        }

        // Notification Switch
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Save notification preference
        }

        // Dark Mode Switch
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Implement dark mode functionality
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}