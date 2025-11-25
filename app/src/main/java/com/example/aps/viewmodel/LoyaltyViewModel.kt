package com.example.aps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aps.api.LoyaltyRepository
import com.example.aps.api.SupabaseClientProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoyaltyUiState(
    val isLoading: Boolean = true,
    val points: Int = 0,
    val error: String? = null
)

class LoyaltyViewModel : ViewModel() {

    private val repo = LoyaltyRepository(SupabaseClientProvider.client)

    private val _state = MutableStateFlow(LoyaltyUiState())
    val state: StateFlow<LoyaltyUiState> = _state.asStateFlow()
    
    // Track if we've completed at least one load
    var hasLoadedOnce = false
        private set

    init {
        load()
    }

    /**
     * Public refresh method that can be called from the UI
     * when the screen becomes visible or needs to reload data
     */
    fun refresh() {
        load()
    }

    private fun load() {
        // Set loading state
        _state.value = _state.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            // Retry logic in case auth isn't ready on first load
            var retryCount = 0
            val maxRetries = 3
            
            while (retryCount < maxRetries) {
                try {
                    val userId = repo.currentUserId()

                    if (userId == null) {
                        // If no user on first attempt, wait a bit for auth to initialize
                        if (retryCount < maxRetries - 1) {
                            retryCount++
                            delay(300) // Wait 300ms before retry
                            continue
                        }
                        
                        _state.value = LoyaltyUiState(
                            isLoading = false,
                            points = 0,
                            error = "No logged-in user"
                        )
                        hasLoadedOnce = true
                        return@launch
                    }

                    val person = repo.getOrCreatePeople(userId)

                    _state.value = LoyaltyUiState(
                        isLoading = false,
                        points = person.loyalty_points,
                        error = null
                    )
                    hasLoadedOnce = true
                    return@launch // Success, exit

                } catch (e: Exception) {
                    e.printStackTrace()
                    
                    // Retry on failure if we have attempts left
                    if (retryCount < maxRetries - 1) {
                        retryCount++
                        delay(300)
                        continue
                    }
                    
                    _state.value = LoyaltyUiState(
                        isLoading = false,
                        points = 0,
                        error = e.message ?: "Unknown error"
                    )
                    hasLoadedOnce = true
                    return@launch
                }
            }
        }
    }
}
