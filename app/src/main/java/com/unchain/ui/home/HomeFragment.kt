package com.unchain.ui.home


import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.unchain.R
import com.unchain.data.preferences.preferences.SugarPreferencesManager
import com.unchain.data.preferences.preferences.UserPreferencesManager
import com.unchain.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var currentCard: View? = null
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            UserPreferencesManager(requireContext()),
            SugarPreferencesManager(requireContext())
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        setupTabs()
        showDailyCard()
    }

    private fun setupObservers() {
        viewModel.userPreferences.observe(viewLifecycleOwner) { userPreferences ->
            binding.userName.text = "${userPreferences.displayName}!"
            if (userPreferences.photoUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(userPreferences.photoUrl)
                    .apply(RequestOptions().transform(RoundedCorners(10)))
                    .into(binding.profileImage)
            }
        }

        viewModel.sugarPreferences.observe(viewLifecycleOwner) { sugarPreferences ->
            // Update sugar amount di card yang sedang aktif
            currentCard?.findViewById<TextView>(R.id.SugarInput)?.let { textView ->
                textView.text = "${sugarPreferences.totalSugarAmount}gr"
            }
        }
    }

    private fun setupTabs() {
        // Set initial state
        updateTabSelection(binding.tabToday)

        binding.tabToday.setOnClickListener {
            updateTabSelection(it)
            showDailyCard()
        }

        binding.tabWeekly.setOnClickListener {
            updateTabSelection(it)
            showWeeklyCard()
        }

        binding.tabMonthly.setOnClickListener {
            updateTabSelection(it)
            showMonthlyCard()
        }
    }

    private fun updateTabSelection(selectedTab: View) {
        // Reset all tabs to unselected state
        binding.tabToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
        binding.tabWeekly.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
        binding.tabMonthly.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))

        // Set selected tab
        (selectedTab as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun animateCardChange(newView: View) {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 150
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                binding.middleCard.removeAllViews()
                binding.middleCard.addView(newView)

                val fadeIn = AlphaAnimation(0f, 1f)
                fadeIn.duration = 150
                binding.middleCard.startAnimation(fadeIn)
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        binding.middleCard.startAnimation(fadeOut)
    }



    private fun showDailyCard() {
        val dailyCardLayout = LayoutInflater.from(requireContext()).inflate(
            R.layout.daily_card_layout,
            binding.middleCard,
            false
        )
        currentCard = dailyCardLayout

        // Update dengan nilai terkini
        viewModel.sugarPreferences.value?.let { prefs ->
            dailyCardLayout.findViewById<TextView>(R.id.SugarInput)?.text =
                "${prefs.totalSugarAmount}gr"

            // Format dan tampilkan tanggal
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd MMM")
            dailyCardLayout.findViewById<TextView>(R.id.currentDate)?.text =
                currentDate.format(formatter)
        }
        animateCardChange(dailyCardLayout)
    }

    private fun showWeeklyCard() {
        val weeklyView = layoutInflater.inflate(R.layout.weekly_card_layout, null)
        currentCard = weeklyView
        // Update dengan nilai terkini
        viewModel.sugarPreferences.value?.let { prefs ->
            weeklyView.findViewById<TextView>(R.id.SugarInput)?.text =
                "${prefs.totalSugarAmount}gr"
        }
        animateCardChange(weeklyView)
    }

    private fun showMonthlyCard() {
        val monthlyView = layoutInflater.inflate(R.layout.monthly_card_layout, null)
        currentCard = monthlyView
        // Update dengan nilai terkini
        viewModel.sugarPreferences.value?.let { prefs ->
            monthlyView.findViewById<TextView>(R.id.SugarInput)?.text =
                "${prefs.totalSugarAmount}gr"
        }
        animateCardChange(monthlyView)
    }



    private fun showAddSugarDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_sugar)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = layoutParams
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnAdd = dialog.findViewById<Button>(R.id.btnAdd)
        val etName = dialog.findViewById<EditText>(R.id.etName)
        val etSugarAmount = dialog.findViewById<EditText>(R.id.etSugarAmount)

        btnCancel.setOnClickListener{
            dialog.dismiss()
        }



        btnAdd.setOnClickListener {
            val foodName = etName.text.toString()
            val sugarAmount = etSugarAmount.text.toString().toIntOrNull()

            when {
                foodName.isEmpty() -> etName.error = "Please enter food name"
                sugarAmount == null -> etSugarAmount.error = "Please enter valid sugar amount"
                else -> {
                    viewModel.addSugarConsumption(foodName, sugarAmount)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun setupClickListeners() {
        binding.btnAdd.setOnClickListener {
            showAddSugarDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}