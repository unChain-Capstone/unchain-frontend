package com.unchain.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.unchain.R
import com.unchain.activities.OnboardingActivity
import com.unchain.data.preferences.model.UserPreferences
import com.unchain.databinding.FragmentSettingsBinding
import com.unchain.ui.home.PremiumFragment
import com.unchain.ui.settings.viewpager.VPAdapter
import com.unchain.viewmodels.CustomTabBar

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var vpAdapter: VPAdapter

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vpAdapter = VPAdapter(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        setupViewPager()
        setupTabBar()
        setupObservers()
        
        // Setup click listeners after ViewPager is ready
        binding.vp.post {
            setupClickListeners()
            setupPremiumButton()
        }

        return binding.root
    }

    private fun setupViewPager() {
        binding.vp.apply {
            adapter = vpAdapter
            isUserInputEnabled = true // Enable swiping between tabs
        }
    }

    private fun setupTabBar() {
        binding.header.tabBar.attachTo(binding.vp)
    }

    private fun setupObservers() {
        viewModel.userPreferences.observe(viewLifecycleOwner) { userPreferences ->
            // Update profile image
            if (userPreferences.photoUrl.isNotEmpty()) {
                var requestOptions = RequestOptions()
                requestOptions = requestOptions.transform(CenterCrop(), RoundedCorners(29))

                Glide.with(this)
                    .load(userPreferences.photoUrl)
                    .apply(RequestOptions().transform(RoundedCorners(10)))
                    .into(binding.header.userProfile)
            }
        }
    }

    private fun setupClickListeners() {
        // Logout
        binding.vp.findViewById<View>(R.id.logout_layout)?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), OnboardingActivity::class.java))
            requireActivity().finish()
        }

        // Support
        binding.vp.findViewById<View>(R.id.support_layout)?.setOnClickListener {
            // TODO: Implement support functionality
        }
    }

    private fun setupPremiumButton() {

        binding.vp.findViewById<View>(R.id.btnSettingsPremium)?.setOnClickListener {
            PremiumFragment().show(childFragmentManager, "premium_dialog")
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