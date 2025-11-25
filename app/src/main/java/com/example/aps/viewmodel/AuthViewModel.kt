package com.example.aps.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aps.api.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {

    // ============================================================
    // REGISTER USER
    // ============================================================
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
                Log.e("REGISTER_DEBUG", "========================================")
                Log.e("REGISTER_DEBUG", "REGISTER FUNCTION CALLED!!!")
                Log.e("REGISTER_DEBUG", "Email: $email, Name: $fullName")
                Log.e("REGISTER_DEBUG", "========================================")

                val sessionManager = SessionManager(context)
                val supabaseAuth = SupabaseClientProvider.client.auth

                val userEmail = email.trim()
                val userPassword = password.trim()

                val userPrefs = context.getSharedPreferences("user_registration", Context.MODE_PRIVATE)
                userPrefs.edit().apply {
                    putString("pending_fullName", fullName)
                    putString("pending_phone", phone)
                    putString("pending_plate", plate)
                    putString("pending_email", userEmail)
                    putBoolean("profile_created", false)
                    apply()
                }

                Log.e("REGISTER_DEBUG", "Stored pending registration data in SharedPreferences")

                // SUPABASE SIGN-UP
                supabaseAuth.signUpWith(Email) {
                    this.email = userEmail
                    this.password = userPassword
                }

                Log.e("REGISTER_DEBUG", "Supabase signup completed")

                val session = supabaseAuth.currentSessionOrNull()
                Log.e("REGISTER_DEBUG", "Session after signup: ${session != null}")

                if (session == null) {
                    Log.e("REGISTER_DEBUG", "Email confirmation required - data already stored")
                    withContext(Dispatchers.Main) { onSuccess() }
                    return@launch
                }

                val uuid = session.user?.id
                val token = session.accessToken

                if (uuid == null || token == null) {
                    Log.e("REGISTER_DEBUG", "ERROR: UUID or token is null")
                    withContext(Dispatchers.Main) {
                        onError("Authentication failed: Invalid session")
                    }
                    return@launch
                }

                sessionManager.saveAccessToken(token)

                val api = RetrofitClient.getClient { sessionManager.getAccessToken() }
                    .create(ApiService::class.java)

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
                        onError("FastAPI error /users/me: ${userResp.code()}")
                    }
                    return@launch
                }

                val peopleResp = api.createPeople(
                    PeopleCreate(
                        plate_number = plate.toIntOrNull() ?: 0,
                        loyalty_points = 0,
                        balance = 0.0
                    )
                )

                if (!peopleResp.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onError("FastAPI error /people: ${peopleResp.code()}")
                    }
                    return@launch
                }

                userPrefs.edit().putBoolean("profile_created", true).apply()

                withContext(Dispatchers.Main) { onSuccess() }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Unknown error") }
            }
        }
    }




    // ============================================================
    // UPDATED LOGIN USER
    // ============================================================
    fun loginUser(
        context: Context,
        email: String,
        password: String,
        onSuccess: (Boolean) -> Unit,   // <-- ADMIN FLAG HERE
        onError: (String) -> Unit
    ) {
        Log.e("LOGIN_DEBUG", "========================================")
        Log.e("LOGIN_DEBUG", "LOGIN FUNCTION CALLED!!!")
        Log.e("LOGIN_DEBUG", "Email: $email")
        Log.e("LOGIN_DEBUG", "========================================")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sessionManager = SessionManager(context)
                val supabaseAuth = SupabaseClientProvider.client.auth

                val userEmail = email.trim()
                val userPassword = password.trim()

                // 1) SUPABASE LOGIN
                supabaseAuth.signInWith(Email) {
                    this.email = userEmail
                    this.password = userPassword
                }

                val session = supabaseAuth.currentSessionOrNull()
                val token = session?.accessToken

                if (session == null || token == null) {
                    withContext(Dispatchers.Main) {
                        onError("Invalid email or password")
                    }
                    return@launch
                }

                sessionManager.saveAccessToken(token)

                // 2) Create API client
                val api = RetrofitClient.getClient { sessionManager.getAccessToken() }
                    .create(ApiService::class.java)

                // 3) Fetch FastAPI user profile (to check admin)
                val userResp = api.getMyProfile()

                if (!userResp.isSuccessful || userResp.body() == null) {
                    withContext(Dispatchers.Main) {
                        onError("Failed to load profile")
                    }
                    return@launch
                }

                val user = userResp.body()!!
                val isAdmin = user.admin   // <-- THIS IS THE FLAG

                Log.e("LOGIN_DEBUG", "User is admin: $isAdmin")

                // 4) SUCCESS — return admin bool
                withContext(Dispatchers.Main) {
                    onSuccess(isAdmin)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Login failed")
                }
            }
        }
    }



    // ============================================================
    // PROFILE FETCH
    // ============================================================
    fun getUserProfile(
        context: Context,
        onSuccess: (UserRead, PeopleRead) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sessionManager = SessionManager(context)
                val api = RetrofitClient.getClient { sessionManager.getAccessToken() }
                    .create(ApiService::class.java)

                val userResp = api.getMyProfile()
                if (!userResp.isSuccessful || userResp.body() == null) {
                    withContext(Dispatchers.Main) { onError("Failed to load user") }
                    return@launch
                }

                val peopleResp = api.getMyPeople()
                if (!peopleResp.isSuccessful || peopleResp.body() == null) {
                    withContext(Dispatchers.Main) { onError("Failed to load people") }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    onSuccess(userResp.body()!!, peopleResp.body()!!)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun logout(context: Context) {
        SessionManager(context).clearSession()
        context.getSharedPreferences("user_registration", Context.MODE_PRIVATE).edit().clear().apply()
    }
}
