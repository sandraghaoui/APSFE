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

                // After signup, check if we have a session
                val session = supabaseAuth.currentSessionOrNull()

                if (session == null) {
                    // Email confirmation required - store data for first login
                    val userPrefs = context.getSharedPreferences("user_registration", Context.MODE_PRIVATE)
                    userPrefs.edit().apply {
                        putString("pending_fullName", fullName)
                        putString("pending_phone", phone)
                        putString("pending_plate", plate)
                        putString("pending_email", userEmail)
                        putBoolean("profile_created", false)
                        apply()
                    }

                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                    return@launch
                }

                // If we reach here, we have a session (email confirmation was disabled)
                val uuid = session.user?.id
                val token = session.accessToken

                if (uuid == null || token == null) {
                    withContext(Dispatchers.Main) {
                        onError("Authentication failed: Invalid session")
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

    fun loginUser(
        context: Context,
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
                // 1) SUPABASE LOGIN
                // ---------------------------------------------
                supabaseAuth.signInWith(Email) {
                    this.email = userEmail
                    this.password = userPassword
                }

                // Get session after login
                val session = supabaseAuth.currentSessionOrNull()
                val token = session?.accessToken

                if (session == null || token == null) {
                    withContext(Dispatchers.Main) {
                        onError("Login failed: Invalid credentials")
                    }
                    return@launch
                }

                // Store JWT token
                sessionManager.saveAccessToken(token)

                // ---------------------------------------------
                // 2) Check if we need to create FastAPI profile
                // ---------------------------------------------
                val userPrefs = context.getSharedPreferences("user_registration", Context.MODE_PRIVATE)
                val profileCreated = userPrefs.getBoolean("profile_created", true)
                val storedEmail = userPrefs.getString("pending_email", "")

                if (!profileCreated && storedEmail == userEmail) {
                    // Get stored registration data
                    val fullName = userPrefs.getString("pending_fullName", "") ?: ""
                    val phone = userPrefs.getString("pending_phone", "") ?: ""
                    val plate = userPrefs.getString("pending_plate", "") ?: ""

                    if (fullName.isNotEmpty()) {
                        // Create FastAPI profiles
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
                                onError("Profile creation failed: ${userResp.code()}")
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
                                onError("People profile creation failed: ${peopleResp.code()}")
                            }
                            return@launch
                        }

                        // Mark profile as created and clear pending data
                        userPrefs.edit().apply {
                            putBoolean("profile_created", true)
                            remove("pending_fullName")
                            remove("pending_phone")
                            remove("pending_plate")
                            remove("pending_email")
                            apply()
                        }
                    }
                }

                // ---------------------------------------------
                // 3) SUCCESS - Navigate to home
                // ---------------------------------------------
                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Login failed")
                }
            }
        }
    }
}