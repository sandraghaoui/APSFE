package com.example.aps.repositories

import com.example.aps.api.ApiService
import com.example.aps.api.ReservationBase
import com.example.aps.api.ReservationRead
import retrofit2.Response

class ReservationRepository(private val api: ApiService) {

    suspend fun updateReservation(
        reservationId: Int,
        status: String? = null,
        checkoutTime: String? = null
    ): Response<ReservationRead> {

        val body = ReservationBase(
            status = status,
            checkout_time = checkoutTime
        )

        return api.updateReservation(reservationId, body)
    }
}
