package com.example.aps.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aps.api.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {

    fun registerUser(
        context: Context,
        fullName: String,
        phone: String,
        plate: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            try {
                val sessionManager = SessionManager(context)
                val supabaseAuth = SupabaseClientProvider.client.auth

                val userEmail = email.trim()
                val userPassword = password.trim()

                // ---------------------------------------------
                // 1) SUPABASE SIGN-UP (correct syntax v3)
                // ---------------------------------------------
                supabaseAuth.signUpWith(Email) {
                    this.email = userEmail
                    this.password = userPassword
                }

                // After signup, fetch current session
                val session = supabaseAuth.currentSessionOrNull()
                val uuid = session?.user?.id
                val token = session?.accessToken

                if (uuid == null || token == null) {
                    withContext(Dispatchers.Main) {
                        onError("Authentication failed: Please check your email to confirm your account")
                    }
                    return@launch
                }

                // Store JWT
                sessionManager.saveAccessToken(token)

                // ---------------------------------------------
                // 2) Create API client with token provider
                // ---------------------------------------------
                val api = RetrofitClient.getClient { sessionManager.getAccessToken() }
                    .create(ApiService::class.java)

                // ---------------------------------------------
                // 3) FASTAPI: create user profile
                // ---------------------------------------------
                val parts = fullName.trim().split(" ", limit = 2)
                val first = parts.firstOrNull() ?: ""
                val last = parts.getOrNull(1) ?: ""

                val userResp = api.createOrUpdateMyProfile(
                    UserCreate(
                        first_name = first,
                        last_name = last,
                        phone_number = phone,
                        email = userEmail,
                        admin = false
                    )
                )

                if (!userResp.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onError("FastAPI error /users/me: ${userResp.code()} - ${userResp.message()}")
                    }
                    return@launch
                }

                // ---------------------------------------------
                // 4) FASTAPI: create people profile
                // ---------------------------------------------
                val peopleResp = api.createPeople(
                    PeopleCreate(
                        plate_number = plate.toIntOrNull() ?: 0,
                        loyalty_points = 0,
                        balance = 0.0
                    )
                )

                if (!peopleResp.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onError("FastAPI error /people: ${peopleResp.code()} - ${peopleResp.message()}")
                    }
                    return@launch
                }

                // ---------------------------------------------
                // 5) SUCCESS
                // ---------------------------------------------
                withContext(Dispatchers.Main) { onSuccess() }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Unknown error during registration")
                }
            }
        }
    }
}