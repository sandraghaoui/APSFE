package com.example.aps.api

data class UserResponse(
    val uuid: String,
    val first_name: String,
    val last_name: String,
    val phone_number: String,
    val email: String,
    val admin: Boolean
)