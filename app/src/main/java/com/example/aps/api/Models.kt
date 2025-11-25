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
@Serializable
data class AdminCreate(
    val dummy: String? = null
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
    val open_time: String,
    val close_time: String,
    val location: String
)

@Serializable
data class ParkingUpdate(
    val current_capacity: Int? = null,
    val maximum_capacity: Int? = null,
    val price_per_hour: Double? = null,
    val open_time: String? = null,
    val close_time: String? = null,
    val location: String? = null
)

@Serializable
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
// RESERVATIONS
// ---------------------
@Serializable
data class ReservationCreate(
    val parking_id: String,
    val time: String,
    val status: String = "Pending",
    val checkout_time: String? = null,
    val price: Double = 0.0
)

@Serializable
data class ReservationRead(
    val id: Int,
    val parking_id: String,
    val people_uuid: String,
    val time: String,
    val status: String,
    val checkout_time: String?,
    val price: Double
)

/**
 * MUST match Python ReservationBase:
 *
 * class ReservationBase(BaseModel):
 *     parking_id: str
 *     time: datetime
 *     status: str = "Pending"
 *     checkout_time: Optional[datetime] = None
 *     price: float = 0.0
 *
 * We make all fields nullable for PATCH (partial update).
 */
@Serializable
data class ReservationBase(
    val parking_id: String? = null,
    val time: String? = null,
    val status: String? = null,
    val checkout_time: String? = null,
    val price: Double? = null
)


// ---------------------
// REVENUES
// ---------------------
@Serializable
data class RevenueBase(
    val date: String,
    val revenue: Double = 0.0,
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
