package com.example.aps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aps.api.ApiService
import com.example.aps.api.ParkingRead
import com.example.aps.api.ParkingUpdate
import com.example.aps.api.PeopleBase
import com.example.aps.api.ReservationCreate
import com.example.aps.api.ReservationRead
import com.example.aps.api.RetrofitClient
import com.example.aps.api.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

                android.util.Log.d("ParkingViewModel", "=== Creating Reservation ===")
                android.util.Log.d("ParkingViewModel", "Parking ID: $parkingId")
                android.util.Log.d("ParkingViewModel", "Time: $time")
                android.util.Log.d("ParkingViewModel", "Checkout Time: $checkoutTime")
                android.util.Log.d("ParkingViewModel", "Status: Confirmed")
                android.util.Log.d("ParkingViewModel", "Price: $price")
                android.util.Log.d("ParkingViewModel", "Request Body: $body")

                val response = api.createReservation(body)
                
                android.util.Log.d("ParkingViewModel", "Response Code: ${response.code()}")
                android.util.Log.d("ParkingViewModel", "Response Success: ${response.isSuccessful}")
                
                if (response.isSuccessful && response.body() != null) {
                    val reservation = response.body()!!
                    android.util.Log.d("ParkingViewModel", "Response Body: $reservation")
                    android.util.Log.d("ParkingViewModel", "✅ Reservation Created Successfully!")
                    android.util.Log.d("ParkingViewModel", "Reservation ID: ${reservation.id}")
                    
                    _bookingState.value = BookingUiState(
                        isLoading = false,
                        reservation = reservation,
                        error = null
                    )
                    withContext(Dispatchers.Main) {
                        android.util.Log.d("ParkingViewModel", "Calling onSuccess callback...")
                        onSuccess(reservation)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    android.util.Log.e("ParkingViewModel", "Response Error Body: $errorBody")
                    
                    val errorMsg = "Failed to create reservation: ${response.code()} - $errorBody"
                    android.util.Log.e("ParkingViewModel", "Error Message: $errorMsg")
                    
                    _bookingState.value = BookingUiState(
                        isLoading = false,
                        reservation = null,
                        error = errorMsg
                    )
                    withContext(Dispatchers.Main) {
                        onError(errorMsg)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("ParkingViewModel", "Exception Creating Reservation", e)
                android.util.Log.e("ParkingViewModel", "Exception Message: ${e.message}")
                android.util.Log.e("ParkingViewModel", "Exception Type: ${e.javaClass.simpleName}")
                
                val errorMsg = e.message ?: "Unknown error"
                _bookingState.value = BookingUiState(
                    isLoading = false,
                    reservation = null,
                    error = errorMsg
                )
                withContext(Dispatchers.Main) {
                    onError(errorMsg)
                }
            }
        }
    }

    fun updateParkingCapacity(parkingName: String, newCapacity: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitClient.getClient { sessionManager.getAccessToken() }
                    .create(ApiService::class.java)

                android.util.Log.d("ParkingViewModel", "")
                android.util.Log.d("ParkingViewModel", "========================================")
                android.util.Log.d("ParkingViewModel", "🔄 UPDATING PARKING CAPACITY")
                android.util.Log.d("ParkingViewModel", "========================================")
                android.util.Log.d("ParkingViewModel", "Parking Name: $parkingName")
                android.util.Log.d("ParkingViewModel", "New Capacity: $newCapacity")
                android.util.Log.d("ParkingViewModel", "Auth Token: ${if (sessionManager.getAccessToken().isNullOrEmpty()) "MISSING" else "Present"}")

                val updateBody = ParkingUpdate(current_capacity = newCapacity)
                android.util.Log.d("ParkingViewModel", "Request Body: $updateBody")
                
                val response = api.updateParking(parkingName, updateBody)
                android.util.Log.d("ParkingViewModel", "Response Code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val updatedParking = response.body()!!
                    android.util.Log.d("ParkingViewModel", "✅ SUCCESS: Parking Capacity Updated!")
                    android.util.Log.d("ParkingViewModel", "Updated Parking: $updatedParking")
                    android.util.Log.d("ParkingViewModel", "Confirmed New Capacity: ${updatedParking.current_capacity}")
                    withContext(Dispatchers.Main) {
                        onComplete()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    android.util.Log.e("ParkingViewModel", "❌ FAILED to update capacity")
                    android.util.Log.e("ParkingViewModel", "Response Code: ${response.code()}")
                    android.util.Log.e("ParkingViewModel", "Error Body: $errorBody")
                    withContext(Dispatchers.Main) {
                        onComplete()
                    }
                }
                android.util.Log.d("ParkingViewModel", "========================================")
                android.util.Log.d("ParkingViewModel", "")
            } catch (e: Exception) {
                android.util.Log.e("ParkingViewModel", "❌ EXCEPTION updating capacity")
                android.util.Log.e("ParkingViewModel", "Exception Type: ${e.javaClass.simpleName}")
                android.util.Log.e("ParkingViewModel", "Exception Message: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    fun updateLoyaltyPoints(pointsToAdd: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitClient.getClient { sessionManager.getAccessToken() }
                    .create(ApiService::class.java)

                android.util.Log.d("ParkingViewModel", "")
                android.util.Log.d("ParkingViewModel", "========================================")
                android.util.Log.d("ParkingViewModel", "🎁 UPDATING LOYALTY POINTS")
                android.util.Log.d("ParkingViewModel", "========================================")
                android.util.Log.d("ParkingViewModel", "Points to Add: $pointsToAdd")
                android.util.Log.d("ParkingViewModel", "Auth Token: ${if (sessionManager.getAccessToken().isNullOrEmpty()) "MISSING" else "Present"}")

                // Get current user profile
                android.util.Log.d("ParkingViewModel", "Step 1: Fetching current profile...")
                val profileResponse = api.getMyPeople()
                android.util.Log.d("ParkingViewModel", "Profile Response Code: ${profileResponse.code()}")
                
                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    val currentProfile = profileResponse.body()!!
                    val newPoints = currentProfile.loyalty_points + pointsToAdd
                    
                    android.util.Log.d("ParkingViewModel", "✅ Profile Fetched Successfully")
                    android.util.Log.d("ParkingViewModel", "Current Profile: $currentProfile")
                    android.util.Log.d("ParkingViewModel", "Current Points: ${currentProfile.loyalty_points}")
                    android.util.Log.d("ParkingViewModel", "Calculated New Points: $newPoints")

                    // Update with new points
                    val updateBody = PeopleBase(
                        plate_number = currentProfile.plate_number,
                        loyalty_points = newPoints,
                        balance = currentProfile.balance
                    )
                    android.util.Log.d("ParkingViewModel", "Step 2: Updating profile...")
                    android.util.Log.d("ParkingViewModel", "Update Body: $updateBody")

                    val updateResponse = api.updateMyPeople(updateBody)
                    android.util.Log.d("ParkingViewModel", "Update Response Code: ${updateResponse.code()}")
                    
                    if (updateResponse.isSuccessful && updateResponse.body() != null) {
                        val updatedProfile = updateResponse.body()!!
                        android.util.Log.d("ParkingViewModel", "✅ SUCCESS: Loyalty Points Updated!")
                        android.util.Log.d("ParkingViewModel", "Updated Profile: $updatedProfile")
                        android.util.Log.d("ParkingViewModel", "Confirmed New Points: ${updatedProfile.loyalty_points}")
                        withContext(Dispatchers.Main) {
                            onComplete()
                        }
                    } else {
                        val errorBody = updateResponse.errorBody()?.string() ?: "No error details"
                        android.util.Log.e("ParkingViewModel", "❌ FAILED to update loyalty points")
                        android.util.Log.e("ParkingViewModel", "Response Code: ${updateResponse.code()}")
                        android.util.Log.e("ParkingViewModel", "Error Body: $errorBody")
                        withContext(Dispatchers.Main) {
                            onComplete()
                        }
                    }
                } else {
                    val errorBody = profileResponse.errorBody()?.string() ?: "No error details"
                    android.util.Log.e("ParkingViewModel", "❌ FAILED to get current profile")
                    android.util.Log.e("ParkingViewModel", "Response Code: ${profileResponse.code()}")
                    android.util.Log.e("ParkingViewModel", "Error Body: $errorBody")
                    withContext(Dispatchers.Main) {
                        onComplete()
                    }
                }
                android.util.Log.d("ParkingViewModel", "========================================")
                android.util.Log.d("ParkingViewModel", "")
            } catch (e: Exception) {
                android.util.Log.e("ParkingViewModel", "❌ EXCEPTION updating loyalty points")
                android.util.Log.e("ParkingViewModel", "Exception Type: ${e.javaClass.simpleName}")
                android.util.Log.e("ParkingViewModel", "Exception Message: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }
}
