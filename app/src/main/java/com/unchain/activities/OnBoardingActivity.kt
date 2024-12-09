package com.unchain.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.unchain.R
import com.unchain.adapters.OnboardingAdapter
import com.unchain.auth.LoginActivity
import com.unchain.data.preferences.preferences.PreferencesManager
import com.unchain.databinding.ActivityOnboardingBinding
import com.unchain.models.OnboardingPage
import com.unchain.viewmodels.OnboardingViewModel

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var preferencesManager: PreferencesManager
    private val viewModel: OnboardingViewModel by viewModels()

    private val onboardingPages = listOf(
        OnboardingPage(
            R.drawable.onboarding1,
            "",
            "Track your daily sugar intake with precision and take control of your health journey one measurement at a time"
        ),
        OnboardingPage(
            R.drawable.onboarding2,
            "",
            "Get personalized insights and recommendations based on your consumption patterns to make informed dietary choices"
        ),
        OnboardingPage(
            R.drawable.onboarding3,
            "",
            "Join a community of health-conscious individuals and start your path to a balanced lifestyle today"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setupViewPager()
        setupIndicators()
        setupButtons()
        observeCurrentPage()
    }

    private fun setupViewPager() {
        val adapter = OnboardingAdapter(onboardingPages)
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.setCurrentPage(position)
            }
        })
    }

    private fun setupIndicators() {
        val indicators = Array(onboardingPages.size) {
            ImageView(this).apply {
                setImageResource(R.drawable.indicator_inactive)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 0, 8, 0)
                }
            }
        }
        indicators[0].setImageResource(R.drawable.indicator_active)
        binding.indicatorLayout.apply {
            indicators.forEach { addView(it) }
        }
    }

    private fun setupButtons() {
        binding.buttonNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < onboardingPages.size - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                preferencesManager.setFirstTimeDone()

                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun observeCurrentPage() {
        viewModel.currentPage.observe(this) { position ->
            updateIndicators(position)
            updateButtonText(position)
        }
    }

    private fun updateIndicators(position: Int) {
        for (i in 0 until binding.indicatorLayout.childCount) {
            val indicator = binding.indicatorLayout.getChildAt(i) as ImageView
            indicator.setImageResource(
                if (i == position) R.drawable.indicator_active
                else R.drawable.indicator_inactive
            )
        }
    }

    private fun updateButtonText(position: Int) {
        binding.buttonNext.apply {
            if (position == onboardingPages.size - 1) {
                text = getString(R.string.sign_in_with_google)
                setCompoundDrawablesWithIntrinsicBounds(R.drawable.google_small, 0, 0, 0)
                gravity = android.view.Gravity.CENTER
                compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.google_icon_padding)
            } else {
                text = getString(R.string.get_started)
                setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                gravity = android.view.Gravity.CENTER
            }
        }
    }
}