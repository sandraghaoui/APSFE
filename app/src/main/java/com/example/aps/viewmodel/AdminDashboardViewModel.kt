package com.example.aps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aps.api.ApiService
import com.example.aps.api.ReservationRead
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardUiState(
    val parkingName: String = "",
    val currentCapacity: Int = 0,
    val maxCapacity: Int = 0,
    val activeReservations: Int = 0,
    val todayRevenue: Double = 0.0,
    val totalCustomers: Int = 0,
    // NEW: occupancy per time slot for today's reservations
    val occupancyByHour: List<Int> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminDashboardViewModel(
    private val api: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState(isLoading = true))
    val state: StateFlow<DashboardUiState> = _state

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)

                // ------------------------------------------------------------
                // 0) Get current admin UUID from /admins/me
                // ------------------------------------------------------------
                val adminResp = api.getMyAdmin()
                if (!adminResp.isSuccessful || adminResp.body() == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Could not load admin profile"
                    )
                    return@launch
                }
                val adminUuid = adminResp.body()!!.uuid

                // ------------------------------------------------------------
                // 1) Pick parking where owner_uuid == adminUuid
                // ------------------------------------------------------------
                val parkingResp = api.listParkings()
                if (!parkingResp.isSuccessful || parkingResp.body().isNullOrEmpty()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "No parkings found"
                    )
                    return@launch
                }

                val allParkings = parkingResp.body()!!
                val parking = allParkings.firstOrNull { it.owner_uuid == adminUuid }
                    ?: allParkings.first() // fallback so app still works if no match

                val parkingName = parking.name

                _state.value = _state.value.copy(
                    parkingName = parkingName,
                    currentCapacity = parking.current_capacity,
                    maxCapacity = parking.maximum_capacity
                )

                // ------------------------------------------------------------
                // 2) Reservations for this parking only
                // ------------------------------------------------------------
                val reservationsResp = api.listReservations()
                var parkingReservations: List<ReservationRead> = emptyList()
                if (reservationsResp.isSuccessful && reservationsResp.body() != null) {
                    val allReservations = reservationsResp.body()!!
                    parkingReservations =
                        filterReservationsForParking(allReservations, parkingName)

                    val active = parkingReservations.count { it.status == "Pending" }
                    _state.value = _state.value.copy(
                        activeReservations = active
                    )
                }

                // we'll reuse "today" both for revenue and hourly occupancy
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // ------------------------------------------------------------
                // 3) Today's revenue for this parking
                // ------------------------------------------------------------
                val revenueResp = api.listRevenues(
                    parkingId = parkingName,
                    start = today,
                    end = today
                )
                if (revenueResp.isSuccessful && revenueResp.body() != null) {
                    val revenues = revenueResp.body()!!
                    val sum = revenues.sumOf { it.revenue }
                    _state.value = _state.value.copy(
                        todayRevenue = sum
                    )
                }

                // ------------------------------------------------------------
                // 4) Total customers (size of /people list)
                // ------------------------------------------------------------
                val peopleResp = api.listPeople()
                if (peopleResp.isSuccessful && peopleResp.body() != null) {
                    _state.value = _state.value.copy(
                        totalCustomers = peopleResp.body()!!.size
                    )
                }

                // ------------------------------------------------------------
                // 5) Compute occupancy per time slot for today's reservations
                // ------------------------------------------------------------
                val occupancyByHour = computeOccupancyByHour(parkingReservations, today)
                _state.value = _state.value.copy(
                    occupancyByHour = occupancyByHour,
                    isLoading = false
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error loading dashboard"
                )
            }
        }
    }

    private fun filterReservationsForParking(
        all: List<ReservationRead>,
        parkingName: String
    ): List<ReservationRead> {
        return all.filter { it.parking_id == parkingName }
    }

    /**
     * We want 8 bars at fixed times:
     *  6, 8, 10, 12, 14, 16, 18, 20
     * For each reservation of TODAY, take its hour from ISO time string,
     * and increment the matching bucket.
     */
    private fun computeOccupancyByHour(
        reservations: List<ReservationRead>,
        today: String
    ): List<Int> {
        // expected ISO format: "YYYY-MM-DDTHH:MM:SS"
        val todayReservations = reservations.filter { it.time.startsWith(today) }

        val hourBuckets = mutableMapOf<Int, Int>()

        for (res in todayReservations) {
            if (res.time.length < 13) continue
            val hourStr = res.time.substring(11, 13)
            val hour = hourStr.toIntOrNull() ?: continue
            hourBuckets[hour] = (hourBuckets[hour] ?: 0) + 1
        }

        val slots = listOf(6, 8, 10, 12, 14, 16, 18, 20)
        return slots.map { h -> hourBuckets[h] ?: 0 }
    }
}
