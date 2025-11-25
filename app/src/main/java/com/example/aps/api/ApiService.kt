package com.example.aps.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // --------------------------
    // USERS
    // --------------------------

    @POST("users/me")
    suspend fun createOrUpdateMyProfile(
        @Body body: UserCreate
    ): Response<UserRead>

    @GET("users/me")
    suspend fun getMyProfile(): Response<UserRead>

    @DELETE("users/me")
    suspend fun deleteMyProfile(): Response<Unit>


    // --------------------------
    // PEOPLE
    // --------------------------

    @GET("people")
    suspend fun listPeople(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<List<PeopleRead>>

    @POST("people")
    suspend fun createPeople(
        @Body body: PeopleCreate
    ): Response<PeopleRead>

    @GET("people/me")
    suspend fun getMyPeople(): Response<PeopleRead>

    @PATCH("people/me")
    suspend fun updateMyPeople(
        @Body body: PeopleBase
    ): Response<PeopleRead>

    @DELETE("people/me")
    suspend fun deleteMyPeople(): Response<Unit>


    // --------------------------
    // ADMINS
    // --------------------------

    @POST("admins")
    suspend fun createAdmin(
        @Body body: AdminCreate
    ): Response<AdminRead>

    @GET("admins")
    suspend fun listAdmins(): Response<List<AdminRead>>

    @GET("admins/me")
    suspend fun getMyAdmin(): Response<AdminRead>

    @DELETE("admins/me")
    suspend fun deleteMyAdmin(): Response<Unit>


    // --------------------------
    // PARKINGS
    // --------------------------

    @POST("parkings")
    suspend fun createParking(
        @Body body: ParkingCreate
    ): Response<ParkingRead>

    @GET("parkings")
    suspend fun listParkings(): Response<List<ParkingRead>>

    @GET("parkings/{name}")
    suspend fun getParking(
        @Path("name") name: String
    ): Response<ParkingRead>

    @DELETE("parkings/{name}")
    suspend fun deleteParking(
        @Path("name") name: String
    ): Response<Unit>


    // --------------------------
    // RESERVATIONS
    // --------------------------

    @POST("reservations")
    suspend fun createReservation(
        @Body body: ReservationCreate
    ): Response<ReservationRead>

    @GET("reservations")
    suspend fun listReservations(
        @Query("parking_id") parkingId: String? = null,
        @Query("people_uuid") peopleUuid: String? = null
    ): Response<List<ReservationRead>>

    @DELETE("reservations/{id}")
    suspend fun deleteReservation(
        @Path("id") id: Int
    ): Response<Unit>


    // --------------------------
    // REVENUES
    // --------------------------

    @GET("revenues")
    suspend fun listRevenues(
        @Query("parking_id") parkingId: String? = null,
        @Query("start") start: String? = null,   // yyyy-mm-dd
        @Query("end") end: String? = null
    ): Response<List<RevenueRead>>

    @POST("revenues")
    suspend fun createRevenue(
        @Body body: RevenueCreate
    ): Response<RevenueRead>

    @GET("revenues/{date}")
    suspend fun getRevenue(
        @Path("date") date: String
    ): Response<RevenueRead>

    @PATCH("revenues/{date}")
    suspend fun updateRevenue(
        @Path("date") date: String,
        @Body body: RevenueBase
    ): Response<RevenueRead>

    @DELETE("revenues/{date}")
    suspend fun deleteRevenue(
        @Path("date") date: String
    ): Response<Unit>
}