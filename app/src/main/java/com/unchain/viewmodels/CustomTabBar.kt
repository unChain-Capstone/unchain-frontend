package com.unchain.viewmodels

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.viewpager2.widget.ViewPager2
import com.unchain.R
import com.unchain.databinding.SettingsTabBarBinding

class CustomTabBar(context: Context?, attrs: AttributeSet?) :
    RelativeLayout(context, attrs) {
        private var binding: SettingsTabBarBinding
        private lateinit var listTabName: List<String>
        private lateinit var listTabTv: List<TextView>
        private var isAnimating = false
        private var onTabSelectedListener: ((Int) -> Unit)? = null
        private var selectedIndex = 0

    init {
        binding = SettingsTabBarBinding.inflate(LayoutInflater.from(context), this, true)
        setupAttrs(attrs)
        setupUI()
    }

    fun attachTo(viewPager: ViewPager2) {
        onTabSelectedListener = {
            viewPager.setCurrentItem(it, true)
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (!isAnimating) {
                    onTabSelected(position)
                }
            }
        })
    }

    private fun setupAttrs(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CustomTabBar,
            0, 0)

        listTabName = typedArray
            .getTextArray(R.styleable.CustomTabBar_android_entries)
            .toList().map {
                it.toString()
            }

        typedArray.recycle()
    }

    private fun setupUI() {
        //textview
        listTabTv = listTabName.mapIndexed { index, tabName ->
            initTabTv(tabName, index)
        }

        //view_tabs_wrapper
        binding.viewTabsWrapper.apply {
            weightSum = listTabTv.size.toFloat()
            listTabTv.forEach {
                this.addView(it)
            }
        }

        //view_indicator_wrapper
        binding.viewIndicatorWrapper.apply {
            weightSum = listTabTv.size.toFloat()
        }
    }

    private fun initTabTv(tabName: String, index: Int) = TextView(context).apply {
        text = tabName
        layoutParams = LinearLayout.LayoutParams(
            0,
            LayoutParams.MATCH_PARENT,
            1f
        )
        gravity = Gravity.CENTER
//        setTextColor(ContextCompat.getColor(this.context, R.color.black))
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD

        setTextColor(if (index == selectedIndex) Color.parseColor("#565766") else Color.parseColor("#7D8594"))

        setOnClickListener {
            selectedIndex = index
            listTabTv.forEachIndexed { i, tabTv ->
                tabTv.setTextColor(if (i == selectedIndex) Color.parseColor("#565766") else Color.parseColor("#7D8594"))
            }
            onTabSelected(index)
        }
    }

    private fun onTabSelected(index: Int) {
        ObjectAnimator.ofFloat(
            binding.viewIndicator,
            View.TRANSLATION_X,
            binding.viewIndicator.x,
            listTabTv[index].x
        ).apply {
            duration = 300
            onTabSelectedListener?.invoke(index)
            isAnimating = true
            start()
            doOnEnd {
                isAnimating = false
            }
        }
    }
}