package com.example.aps.api

// ---------------------
// USERS
// ---------------------
data class UserCreate(
    val first_name: String,
    val last_name: String,
    val phone_number: String,
    val email: String,
    val admin: Boolean = false
)

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
data class PeopleBase(
    val plate_number: Int?,
    val loyalty_points: Int = 0,
    val balance: Double = 0.0
)

data class PeopleCreate(
    val plate_number: Int?,
    val loyalty_points: Int = 0,
    val balance: Double = 0.0
)

data class PeopleRead(
    val uuid: String,
    val plate_number: Int?,
    val loyalty_points: Int,
    val balance: Double
)


// ---------------------
// ADMINS
// ---------------------
data class AdminCreate(
    val uuid: String
)

data class AdminRead(
    val uuid: String
)


// ---------------------
// PARKING
// ---------------------
data class ParkingCreate(
    val name: String,
    val owner_uuid: String,
    val current_capacity: Int = 0,
    val maximum_capacity: Int,
    val price_per_hour: Double,
    val open_time: String,   // ISO datetime string
    val close_time: String,
    val location: String
)

data class ParkingRead(
    val name: String,
    val owner_uuid: String,
    val current_capacity: Int,
    val maximum_capacity: Int,
    val price_per_hour: Double,
    val open_time: String,
    val close_time: String,
    val location: String
)


// ---------------------
// RESERVATION
// ---------------------
data class ReservationCreate(
    val parking_id: String,
    val people_uuid: String,
    val time: String,
    val status: String = "Pending",
    val checkout_time: String? = null,
    val price: Double = 0.0
)

data class ReservationRead(
    val id: Int,
    val parking_id: String,
    val people_uuid: String,
    val time: String,
    val status: String,
    val checkout_time: String?,
    val price: Double
)


// ---------------------
// REVENUES
// ---------------------
data class RevenueBase(
    val revenue: Double = 0.0,
    val parking_id: String
)

data class RevenueCreate(
    val date: String,
    val revenue: Double = 0.0,
    val parking_id: String
)

data class RevenueRead(
    val date: String,
    val revenue: Double,
    val parking_id: String
)
