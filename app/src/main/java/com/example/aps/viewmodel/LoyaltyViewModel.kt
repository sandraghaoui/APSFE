package com.example.aps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aps.api.LoyaltyRepository
import com.example.aps.api.SupabaseClientProvider
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

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val userId = repo.currentUserId()

                if (userId == null) {
                    _state.value = LoyaltyUiState(
                        isLoading = false,
                        points = 0,
                        error = "No logged-in user"
                    )
                    return@launch
                }

                val person = repo.getOrCreatePeople(userId)

                _state.value = LoyaltyUiState(
                    isLoading = false,
                    points = person.loyalty_points,
                    error = null
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = LoyaltyUiState(
                    isLoading = false,
                    points = 0,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}
