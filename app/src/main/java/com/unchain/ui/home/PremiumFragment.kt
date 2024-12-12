package com.unchain.ui.home

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.unchain.R

class PremiumFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.premium_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply slide up animation to the root view
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        view.startAnimation(slideUp)

        setupPremiumButton(view)
        setupCloseButton(view)
    }

    private fun setupPremiumButton(view: View) {
        val btnTryPremium = view.findViewById<AppCompatButton>(R.id.btnTryPremium)
        
        // Create a SpannableString with both normal and strike-through text
        val normalText = "Try Premium for IDR0 "
        val strikeText = "IDR50.000"
        val fullText = normalText + strikeText
        
        val spannableString = SpannableString(fullText)
        
        // Apply strike-through and color to the price part
        val start = normalText.length
        val end = fullText.length
        
        // Add strike-through
        spannableString.setSpan(
            StrikethroughSpan(),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // Add different color (grey)
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.dark_grey)),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        btnTryPremium.text = spannableString
    }

    private fun setupCloseButton(view: View) {
        view.findViewById<View>(R.id.btnClose).setOnClickListener {
            // Apply slide down animation before dismissing
            val slideDown = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
            slideDown.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    dismiss()
                }
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
            view.startAnimation(slideDown)
        }
    }
}
