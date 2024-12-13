package com.unchain.ui.behavior

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unchain.data.model.BehaviorResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class BehaviorViewModel @Inject constructor(
    private val behaviorRepository: BehaviorRepository
) : ViewModel() {

    sealed class BehaviorState {
        object Loading : BehaviorState()
        data class Success(val data: BehaviorResponse) : BehaviorState()
        data class Error(val message: String) : BehaviorState()
    }

    private val _behaviorState = MutableStateFlow<BehaviorState>(BehaviorState.Loading)
    val behaviorState: StateFlow<BehaviorState> = _behaviorState
    private val TAG = "BehaviorViewModel"

    fun fetchBehavior() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching behavior data")
                _behaviorState.value = BehaviorState.Loading
                val response = behaviorRepository.getBehavior()
                Log.d(TAG, "Behavior data received: $response")
                _behaviorState.value = BehaviorState.Success(response)
            } catch (e: HttpException) {
                Log.e(TAG, "Error fetching behavior data", e)
                _behaviorState.value = BehaviorState.Error(e.message ?: "Network error occurred")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching behavior data", e)
                _behaviorState.value = BehaviorState.Error(e.message ?: "An error occurred")
            }
        }
    }
}
