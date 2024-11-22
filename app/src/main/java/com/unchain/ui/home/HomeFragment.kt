package com.unchain.ui.home

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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
            binding.SugarInput.text = "${sugarPreferences.totalSugarAmount}gr"
        }
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

        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val btnAdd = dialog.findViewById<Button>(R.id.btnAdd)
        val etName = dialog.findViewById<EditText>(R.id.etName)
        val etSugarAmount = dialog.findViewById<EditText>(R.id.etSugarAmount)

        btnClose.setOnClickListener {
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
        binding.btnAddSugar.setOnClickListener {
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