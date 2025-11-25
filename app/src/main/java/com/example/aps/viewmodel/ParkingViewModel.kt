package com.example.aps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aps.api.ApiService
import com.example.aps.api.ParkingRead
import com.example.aps.api.ReservationCreate
import com.example.aps.api.ReservationRead
import com.example.aps.api.RetrofitClient
import com.example.aps.api.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ParkingUiState(
    val isLoading: Boolean = true,
    val parkings: List<ParkingRead> = emptyList(),
    val error: String? = null
)

data class BookingUiState(
    val isLoading: Boolean = false,
    val reservation: ReservationRead? = null,
    val error: String? = null
)

class ParkingViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _parkingState = MutableStateFlow(ParkingUiState())
    val parkingState: StateFlow<ParkingUiState> = _parkingState.asStateFlow()

    private val _bookingState = MutableStateFlow(BookingUiState())
    val bookingState: StateFlow<BookingUiState> = _bookingState.asStateFlow()

    init {
        loadParkings()
    }

    fun loadParkings() {
        viewModelScope.launch(Dispatchers.IO) {
            _parkingState.value = ParkingUiState(isLoading = true)
            try {
                val api = RetrofitClient.getClient { sessionManager.getAccessToken() }
                    .create(ApiService::class.java)
                
                val response = api.listParkings()
                if (response.isSuccessful && response.body() != null) {
                    _parkingState.value = ParkingUiState(
                        isLoading = false,
                        parkings = response.body()!!,
                        error = null
                    )
                } else {
                    _parkingState.value = ParkingUiState(
                        isLoading = false,
                        parkings = emptyList(),
                        error = "Failed to load parkings: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _parkingState.value = ParkingUiState(
                    isLoading = false,
                    parkings = emptyList(),
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun createReservation(
        parkingId: String,
        time: String,
        checkoutTime: String?,
        price: Double,
        onSuccess: (ReservationRead) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _bookingState.value = BookingUiState(isLoading = true)
            try {
                val api = RetrofitClient.getClient { sessionManager.getAccessToken() }
                    .create(ApiService::class.java)

                val body = ReservationCreate(
                    parking_id = parkingId,
                    time = time,
                    status = "Confirmed",
                    checkout_time = checkoutTime,
                    price = price
                )

                val response = api.createReservation(body)
                if (response.isSuccessful && response.body() != null) {
                    val reservation = response.body()!!
                    _bookingState.value = BookingUiState(
                        isLoading = false,
                        reservation = reservation,
                        error = null
                    )
                    onSuccess(reservation)
                } else {
                    val errorMsg = "Failed to create reservation: ${response.code()}"
                    _bookingState.value = BookingUiState(
                        isLoading = false,
                        reservation = null,
                        error = errorMsg
                    )
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = e.message ?: "Unknown error"
                _bookingState.value = BookingUiState(
                    isLoading = false,
                    reservation = null,
                    error = errorMsg
                )
                onError(errorMsg)
            }
        }
    }
}
