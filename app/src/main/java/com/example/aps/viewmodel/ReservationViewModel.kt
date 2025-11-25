package com.example.aps.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aps.repositories.ReservationRepository
import kotlinx.coroutines.launch

class ReservationViewModel(
    private val repo: ReservationRepository
) : ViewModel() {

    fun updateReservation(
        id: Int,
        status: String?,
        checkoutTime: String?,
        onDone: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val res = repo.updateReservation(id, status, checkoutTime)
                if (res.isSuccessful) {
                    onDone(true, null)
                } else {
                    onDone(false, "Server error: ${res.code()}")
                }
            } catch (e: Exception) {
                onDone(false, e.message)
            }
        }
    }
}
