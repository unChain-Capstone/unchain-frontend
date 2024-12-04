package com.unchain.ui.home

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.app.Dialog
import android.content.Context
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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.unchain.R
import com.unchain.data.preferences.preferences.SugarPreferencesManager
import com.unchain.data.preferences.preferences.UserPreferencesManager
import com.unchain.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.unchain.data.model.SugarHistory
import com.unchain.network.ApiClient
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import androidx.recyclerview.widget.LinearLayoutManager
import com.unchain.data.model.HistoryResponse
import com.unchain.utils.hideLoading
import com.unchain.utils.showLoading

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
    private lateinit var dailyConsumeAdapter: DailyConsumeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        setupTabs()
        showDailyCard()

        dailyConsumeAdapter = DailyConsumeAdapter()
        binding.rvDailyConsume.apply {
            adapter = dailyConsumeAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Observe history data
        viewModel.historyData.observe(viewLifecycleOwner) { histories ->
            dailyConsumeAdapter.setItems(histories)
        }

        // Load histories
        viewModel.loadHistories()
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

    private fun loadDailyConsumption() {
        viewModel.loadHistories(forceRefresh = true)
    }

    private fun showAddSugarDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_sugar)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        dialog.window?.attributes = layoutParams
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnAdd = dialog.findViewById<Button>(R.id.btnAdd)
        val etName = dialog.findViewById<EditText>(R.id.etName)
        val etSugarAmount = dialog.findViewById<EditText>(R.id.etSugarAmount)
        val btnFood = dialog.findViewById<Button>(R.id.btnFood)
        val btnBeverage = dialog.findViewById<Button>(R.id.btnBeverage)

        // Set IME options
        etName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                etSugarAmount.requestFocus()
                true
            } else {
                false
            }
        }

        etSugarAmount.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etSugarAmount.windowToken, 0)
                true
            } else {
                false
            }
        }

        var isBeverage = false

        fun updateButtonStates(isFood: Boolean) {
            isBeverage = !isFood
            if (isFood) {
                btnFood.setBackgroundResource(R.drawable.button_toggle_selected)
                btnFood.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                btnBeverage.setBackgroundResource(R.drawable.button_toggle_unselected)
                btnBeverage.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            } else {
                btnBeverage.setBackgroundResource(R.drawable.button_toggle_selected)
                btnBeverage.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                btnFood.setBackgroundResource(R.drawable.button_toggle_unselected)
                btnFood.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }

        // Set initial button states
        updateButtonStates(true)

        btnFood.setOnClickListener {
            updateButtonStates(true)
        }

        btnBeverage.setOnClickListener {
            updateButtonStates(false)
        }

        btnCancel.setOnClickListener {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(dialog.currentFocus?.windowToken, 0)
            dialog.dismiss()
        }

        btnAdd.setOnClickListener {
            val foodName = etName.text.toString()
            val sugarAmount = etSugarAmount.text.toString().toIntOrNull()

            when {
                foodName.isEmpty() -> etName.error = "Please enter food name"
                sugarAmount == null -> etSugarAmount.error = "Please enter valid sugar amount"
                else -> {
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(dialog.currentFocus?.windowToken, 0)
                    
                    val loadingDialog = showLoading()
                    
                    val history = SugarHistory(
                        title = foodName,
                        weight = sugarAmount,
                        isBeverage = isBeverage
                    )
                    
                    ApiClient.apiService.addHistory(history)
                        .enqueue(object : Callback<SugarHistory> {
                            override fun onResponse(call: Call<SugarHistory>, response: Response<SugarHistory>) {
                                loadingDialog.hideLoading()
                                if (response.isSuccessful) {
                                    // Handle successful response
                                    dialog.dismiss()
                                    // Refresh data or show success message
                                    viewModel.addSugarConsumption(foodName, sugarAmount)
                                    loadDailyConsumption() // Refresh the list
                                    Log.d("HomeFragment", "Successfully saved sugar history")
                                } else {
                                    // Handle error
                                    Toast.makeText(requireContext(), "Failed to add history", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<SugarHistory>, t: Throwable) {
                                loadingDialog.hideLoading()
                                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
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

    private fun deleteHistory(historyId: Int) {
        val loadingDialog = showLoading()
        ApiClient.apiService.deleteHistory(historyId)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    loadingDialog.hideLoading()
                    if (response.isSuccessful) {
                        // Refresh the list after successful deletion
                        loadDailyConsumption()
                        Toast.makeText(
                            requireContext(),
                            "History deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to delete history",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    loadingDialog.hideLoading()
                    Toast.makeText(
                        requireContext(),
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
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