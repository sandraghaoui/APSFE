package com.example.aps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aps.api.LoyaltyRepository
import com.example.aps.api.SupabaseClientProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoyaltyUiState(
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false,
    val points: Int = 0
)

class LoyaltyViewModel : ViewModel() {

    private val repo = LoyaltyRepository(SupabaseClientProvider.client)

    private val _state = MutableStateFlow(LoyaltyUiState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val userId = repo.currentUserId()

            if (userId == null) {
                _state.value = LoyaltyUiState(isLoading = false)
                return@launch
            }

            val person = repo.getOrCreatePeopleIfNotAdmin(userId)

            _state.value = LoyaltyUiState(
                isLoading = false,
                isAdmin = (person == null),
                points = person?.loyalty_points ?: 0
            )
        }
    }
}