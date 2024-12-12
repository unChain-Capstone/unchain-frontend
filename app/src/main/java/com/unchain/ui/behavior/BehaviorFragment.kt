package com.unchain.ui.behavior

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.unchain.data.model.BehaviorData
import com.unchain.data.model.BehaviorResponse
import com.unchain.databinding.FragmentBehaviorBinding
import com.unchain.utils.showLoading
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class BehaviorFragment : Fragment() {
    private var _binding: FragmentBehaviorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BehaviorViewModel by viewModels()
    private val TAG = "BehaviorFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBehaviorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Setting up observers and fetching behavior")
        setupObservers()
        setupClickListeners()
        viewModel.fetchBehavior()
    }

    private fun setupClickListeners() {
        binding.btnInputData.setOnClickListener {
            // TODO: Navigate to input data screen
            Snackbar.make(binding.root, "Coming soon: Input data screen", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnViewHistory.setOnClickListener {
            // TODO: Navigate to history screen
            Snackbar.make(binding.root, "Coming soon: History screen", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.behaviorState.collect { state ->
                Log.d(TAG, "State changed to: $state")
                when (state) {
                    is BehaviorState.Loading -> {
                        Log.d(TAG, "Loading state")
                        showLoading(true)
                    }
                    is BehaviorState.Success -> {
                        Log.d(TAG, "Success state: ${state.data}")
                        showLoading(false)
                        updateUI(state.data.data)
                    }
                    is BehaviorState.Error -> {
                        Log.e(TAG, "Error state: ${state.message}")
                        showLoading(false)
                        binding.tvBehaviorStatus.text = "Error"
                        binding.tvMessage.text = state.message
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun updateUI(data: BehaviorData) {
        binding.apply {
            tvBehaviorStatus.text = data.behaviourStatus
            tvMessage.text = data.message
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.isVisible = isLoading
            ivStatusIcon.isVisible = !isLoading
            tvBehaviorStatus.isVisible = !isLoading
            tvMessage.isVisible = !isLoading
            divider.isVisible = !isLoading
            tvActionTitle.isVisible = !isLoading
            btnInputData.isVisible = !isLoading
            btnViewHistory.isVisible = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
