package com.unchain.ui.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.unchain.R
import com.unchain.data.model.BehaviorResponse
import com.unchain.databinding.FragmentBehaviorBinding
import com.unchain.ui.behavior.BehaviorViewModel.BehaviorState
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
        setupObservers()
        setupClickListeners()
        viewModel.fetchBehavior()
    }

    private fun setupClickListeners() {
        binding.btnInputData.setOnClickListener {
            findNavController().navigate(R.id.action_behaviorFragment_to_navigation_home)
        }

        binding.btnViewHistory.setOnClickListener {
            findNavController().navigate(R.id.action_behaviorFragment_to_navigation_home)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.behaviorState.collect { state ->
                when (state) {
                    BehaviorState.Loading -> {
                        showLoading(true)
                    }
                    is BehaviorState.Success -> {
                        showLoading(false)
                        updateUI(state.data)
                    }
                    is BehaviorState.Error -> {
                        showLoading(false)
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateUI(data: BehaviorResponse) {
        with(binding) {
            // Update status and message
            tvBehaviorStatus.text = data.data.behaviourStatus
            tvMessage.text = data.data.message

            // Update status icon and color based on status
            when (data.data.behaviourStatus.lowercase(Locale.getDefault())) {
                "moderate addiction" -> {
                    ivStatusIcon.setImageResource(R.drawable.ic_warning)
                    ivStatusIcon.setColorFilter(resources.getColor(R.color.warning, null))
                }
                "high addiction" -> {
                    ivStatusIcon.setImageResource(R.drawable.ic_warning)
                    ivStatusIcon.setColorFilter(resources.getColor(R.color.error, null))
                }
                "low addiction" -> {
                    ivStatusIcon.setImageResource(R.drawable.ic_warning)
                    ivStatusIcon.setColorFilter(resources.getColor(R.color.success, null))
                }
                else -> {
                    ivStatusIcon.setImageResource(R.drawable.ic_warning)
                    ivStatusIcon.setColorFilter(resources.getColor(R.color.warning, null))
                }
            }

            // Show suggestions sections
            tvTop3Suggestion.visibility = View.VISIBLE
            llTop3Suggestions.visibility = View.VISIBLE
            tvOtherSuggestion.visibility = View.VISIBLE
            llOtherSuggestions.visibility = View.VISIBLE

            // Show main content
            mainContainer.visibility = if (progressBar.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    private fun showLoading(show: Boolean) {
        binding.apply {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
            mainContainer.visibility = if (show) View.INVISIBLE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
