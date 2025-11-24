package com.example.aps.api
import kotlinx.serialization.Serializable


// ---------------------
// USERS
// ---------------------
@Serializable
data class UserCreate(
    val first_name: String,
    val last_name: String,
    val phone_number: String,
    val email: String,
    val admin: Boolean = false
)
@Serializable
data class UserRead(
    val uuid: String,
    val first_name: String,
    val last_name: String,
    val phone_number: String,
    val email: String,
    val admin: Boolean
)


// ---------------------
// PEOPLE
// ---------------------
@Serializable
data class PeopleBase(
    val plate_number: Int,
    val loyalty_points: Int = 0,
    val balance: Double = 0.0
)
@Serializable
data class PeopleCreate(
    val plate_number: Int,
    val loyalty_points: Int = 0,
    val balance: Double = 0.0
    // uuid comes from JWT token
)
@Serializable
data class PeopleRead(
    val uuid: String,
    val plate_number: Int,
    val loyalty_points: Int,
    val balance: Double
)


// ---------------------
// ADMINS
// ---------------------
// Backend AdminCreate is empty (UUID from token)
// We can omit body or send empty object
@Serializable
data class AdminCreate(
    val dummy: String? = null  // Optional, not used by backend
)
@Serializable
data class AdminRead(
    val uuid: String
)


// ---------------------
// PARKING
// ---------------------
@Serializable
data class ParkingCreate(
    val name: String,
    val current_capacity: Int = 0,
    val maximum_capacity: Int,
    val price_per_hour: Double,
    val open_time: String,   // ISO datetime: "2025-11-24T10:00:00"
    val close_time: String,  // ISO datetime: "2025-11-24T22:00:00"
    val location: String
    // owner_uuid auto-filled by backend from token
)
@Serializable
data class ParkingRead(
    val name: String,
    val owner_uuid: String,
    val current_capacity: Int,
    val maximum_capacity: Int,
    val price_per_hour: Double,
    val open_time: String,   // Backend sends datetime, will be string
    val close_time: String,
    val location: String
)


// ---------------------
// RESERVATION
// ---------------------
@Serializable
data class ReservationCreate(
    val parking_id: String,
    val time: String,  // ISO datetime: "2025-11-24T10:00:00"
    val status: String = "Pending",
    val checkout_time: String? = null,  // ISO datetime or null
    val price: Double = 0.0
    // people_uuid auto-filled by backend from token
)
@Serializable
data class ReservationRead(
    val id: Int,  // Backend uses "id" not "reservation_id"
    val parking_id: String,
    val people_uuid: String,
    val time: String,  // Backend sends datetime as string
    val status: String,
    val checkout_time: String?,
    val price: Double
)


// ---------------------
// REVENUES
// ---------------------
@Serializable
data class RevenueBase(
    val date: String,  // ISO date: "2025-11-24"
    val revenue: Double = 0.0,  // Backend uses "revenue" not "amount"
    val parking_id: String
)
@Serializable
data class RevenueCreate(
    val date: String,
    val revenue: Double = 0.0,
    val parking_id: String
)
@Serializable
data class RevenueRead(
    val date: String,
    val revenue: Double,
    val parking_id: String
)