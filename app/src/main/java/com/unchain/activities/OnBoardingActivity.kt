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
import com.unchain.databinding.ActivityOnboardingBinding
import com.unchain.models.OnboardingPage
import com.unchain.viewmodels.OnboardingViewModel

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private val viewModel: OnboardingViewModel by viewModels()

    private val onboardingPages = listOf(
        OnboardingPage(
            R.drawable.group,
            "Welcome",
            "Welcome to our amazing app!"
        ),
        OnboardingPage(
            R.drawable.group,
            "Features",
            "Discover all the amazing features"
        ),
        OnboardingPage(
            R.drawable.group,
            "Get Started",
            "Let's begin your journey"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                // Nanti Bisa di ganti ke Login page ya mam
                startActivity(Intent(this, MainActivity::class.java))
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
        binding.buttonNext.text = if (position == onboardingPages.size - 1) "Sign In With Google" else "Get Started"
    }
}