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

                // Store registration data FIRST - before any Supabase calls
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

                // After signup, check if we have a session
                val session = supabaseAuth.currentSessionOrNull()

                Log.e("REGISTER_DEBUG", "Session after signup: ${if (session != null) "exists" else "null"}")

                if (session == null) {
                    // Email confirmation required
                    Log.e("REGISTER_DEBUG", "Email confirmation required - data already stored")
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                    return@launch
                }

                // If we reach here, we have a session (email confirmation was disabled or already confirmed)
                val uuid: String? = session.user?.id
                val token: String? = session.accessToken

                if (uuid == null || token == null) {
                    Log.e("REGISTER_DEBUG", "ERROR: UUID or token is null")
                    withContext(Dispatchers.Main) {
                        onError("Authentication failed: Invalid session")
                    }
                    return@launch
                }

                Log.e("REGISTER_DEBUG", "Session obtained, creating profiles immediately...")

                // Store JWT
                sessionManager.saveAccessToken(token)

                // Create API client with token provider
                val api = RetrofitClient.getClient { sessionManager.getAccessToken() }
                    .create(ApiService::class.java)

                // FASTAPI: create user profile
                val parts = fullName.trim().split(" ", limit = 2)
                val first = parts.firstOrNull() ?: ""
                val last = parts.getOrNull(1) ?: ""

                Log.e("REGISTER_DEBUG", "Creating user profile: $first $last")

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
                    Log.e("REGISTER_DEBUG", "User profile creation failed: ${userResp.code()}")
                    withContext(Dispatchers.Main) {
                        onError("FastAPI error /users/me: ${userResp.code()} - ${userResp.message()}")
                    }
                    return@launch
                }

                Log.e("REGISTER_DEBUG", "User profile created successfully")

                // FASTAPI: create people profile
                Log.e("REGISTER_DEBUG", "Creating people profile...")

                val peopleResp = api.createPeople(
                    PeopleCreate(
                        plate_number = plate.toIntOrNull() ?: 0,
                        loyalty_points = 0,
                        balance = 0.0
                    )
                )

                if (!peopleResp.isSuccessful) {
                    Log.e("REGISTER_DEBUG", "People profile creation failed: ${peopleResp.code()}")
                    withContext(Dispatchers.Main) {
                        onError("FastAPI error /people: ${peopleResp.code()} - ${peopleResp.message()}")
                    }
                    return@launch
                }

                Log.e("REGISTER_DEBUG", "People profile created successfully")

                // Mark profile as created
                userPrefs.edit().apply {
                    putBoolean("profile_created", true)
                    apply()
                }

                Log.e("REGISTER_DEBUG", "Marked profile as created")

                // SUCCESS
                withContext(Dispatchers.Main) {
                    Log.e("REGISTER_DEBUG", "Registration complete!")
                    onSuccess()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("REGISTER_DEBUG", "Registration error: ${e.message}", e)
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
        Log.e("LOGIN_DEBUG", "========================================")
        Log.e("LOGIN_DEBUG", "LOGIN FUNCTION CALLED!!!")
        Log.e("LOGIN_DEBUG", "Email: $email")
        Log.e("LOGIN_DEBUG", "========================================")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.e("LOGIN_DEBUG", "Inside coroutine, starting login...")

                val sessionManager = SessionManager(context)
                val supabaseAuth = SupabaseClientProvider.client.auth

                val userEmail = email.trim()
                val userPassword = password.trim()

                Log.e("LOGIN_DEBUG", "About to call Supabase signInWith...")

                // 1) SUPABASE LOGIN
                supabaseAuth.signInWith(Email) {
                    this.email = userEmail
                    this.password = userPassword
                }

                Log.e("LOGIN_DEBUG", "Supabase login completed, getting session...")

                // Get session after login
                val session = supabaseAuth.currentSessionOrNull()
                val token = session?.accessToken

                if (session == null || token == null) {
                    Log.e("LOGIN_DEBUG", "ERROR: Session or token is null!")
                    withContext(Dispatchers.Main) {
                        onError("Login failed: Invalid credentials")
                    }
                    return@launch
                }

                Log.e("LOGIN_DEBUG", "Session obtained successfully")

                // Store JWT token
                sessionManager.saveAccessToken(token)

                // 2) Check if we need to create FastAPI profile
                val userPrefs = context.getSharedPreferences("user_registration", Context.MODE_PRIVATE)
                val profileCreated = userPrefs.getBoolean("profile_created", false)
                val storedEmail = userPrefs.getString("pending_email", "")

                Log.e("LOGIN_DEBUG", "Profile check - profileCreated: $profileCreated")
                Log.e("LOGIN_DEBUG", "Profile check - storedEmail: $storedEmail")
                Log.e("LOGIN_DEBUG", "Profile check - currentEmail: $userEmail")

                if (!profileCreated && storedEmail == userEmail) {
                    Log.e("LOGIN_DEBUG", "CREATING PROFILE FOR FIRST-TIME LOGIN...")

                    // Get stored registration data
                    val fullName = userPrefs.getString("pending_fullName", "") ?: ""
                    val phone = userPrefs.getString("pending_phone", "") ?: ""
                    val plate = userPrefs.getString("pending_plate", "") ?: ""

                    Log.e("LOGIN_DEBUG", "Registration data - Name: $fullName, Phone: $phone, Plate: $plate")

                    if (fullName.isNotEmpty()) {
                        // Create FastAPI profiles
                        val api = RetrofitClient.getClient { sessionManager.getAccessToken() }
                            .create(ApiService::class.java)

                        val parts = fullName.trim().split(" ", limit = 2)
                        val first = parts.firstOrNull() ?: ""
                        val last = parts.getOrNull(1) ?: ""

                        Log.e("LOGIN_DEBUG", "Creating user profile: $first $last")
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
                            Log.e("LOGIN_DEBUG", "User profile creation failed: ${userResp.code()} - ${userResp.errorBody()?.string()}")
                            withContext(Dispatchers.Main) {
                                onError("Profile creation failed: ${userResp.code()}")
                            }
                            return@launch
                        }

                        Log.e("LOGIN_DEBUG", "User profile created successfully")
                        Log.e("LOGIN_DEBUG", "Creating people profile...")

                        val peopleResp = api.createPeople(
                            PeopleCreate(
                                plate_number = plate.toIntOrNull() ?: 0,
                                loyalty_points = 0,
                                balance = 0.0
                            )
                        )

                        if (!peopleResp.isSuccessful) {
                            Log.e("LOGIN_DEBUG", "People profile creation failed: ${peopleResp.code()} - ${peopleResp.errorBody()?.string()}")
                            withContext(Dispatchers.Main) {
                                onError("People profile creation failed: ${peopleResp.code()}")
                            }
                            return@launch
                        }

                        Log.e("LOGIN_DEBUG", "People profile created successfully")

                        // Mark profile as created and clear pending data
                        userPrefs.edit().apply {
                            putBoolean("profile_created", true)
                            remove("pending_fullName")
                            remove("pending_phone")
                            remove("pending_plate")
                            remove("pending_email")
                            apply()
                        }

                        Log.e("LOGIN_DEBUG", "Profile creation complete, cleared pending data")
                    } else {
                        Log.e("LOGIN_DEBUG", "Full name is empty, skipping profile creation")
                    }
                } else {
                    Log.e("LOGIN_DEBUG", "Profile already exists or no pending data - skipping creation")
                }

                // 3) SUCCESS - Navigate to home
                Log.e("LOGIN_DEBUG", "Login complete, calling onSuccess callback")
                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("LOGIN_DEBUG", "Login error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Login failed")
                }
            }
        }
    }
    fun getUserProfile(
        context: Context,
        onSuccess: (UserRead, PeopleRead) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sessionManager = SessionManager(context)
                val token = sessionManager.getAccessToken()

                Log.e("PROFILE_DEBUG", "========================================")
                Log.e("PROFILE_DEBUG", "Fetching profile...")
                Log.e("PROFILE_DEBUG", "Token exists: ${token != null}")
                Log.e("PROFILE_DEBUG", "Token length: ${token?.length}")

                // Decode the JWT to see the user UUID
                if (token != null) {
                    try {
                        val parts = token.split(".")
                        if (parts.size >= 2) {
                            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
                            Log.e("PROFILE_DEBUG", "Token payload: $payload")
                        }
                    } catch (e: Exception) {
                        Log.e("PROFILE_DEBUG", "Could not decode token: ${e.message}")
                    }
                }

                val api = RetrofitClient.getClient { sessionManager.getAccessToken() }
                    .create(ApiService::class.java)

                // Fetch user profile
                Log.e("PROFILE_DEBUG", "Calling GET /users/me...")
                val userResp = api.getMyProfile()
                Log.e("PROFILE_DEBUG", "User response code: ${userResp.code()}")

                if (!userResp.isSuccessful) {
                    val errorBody = userResp.errorBody()?.string() ?: "Unknown error"
                    Log.e("PROFILE_DEBUG", "User fetch failed: $errorBody")
                    withContext(Dispatchers.Main) {
                        onError("Failed to load user profile: $errorBody")
                    }
                    return@launch
                }

                val user = userResp.body()
                if (user == null) {
                    Log.e("PROFILE_DEBUG", "User body is null")
                    withContext(Dispatchers.Main) {
                        onError("User profile data is empty")
                    }
                    return@launch
                }

                Log.e("PROFILE_DEBUG", "User profile fetched successfully: ${user.uuid}")

                // Fetch people profile
                Log.e("PROFILE_DEBUG", "Calling GET /people/me...")
                val peopleResp = api.getMyPeople()
                Log.e("PROFILE_DEBUG", "People response code: ${peopleResp.code()}")

                if (!peopleResp.isSuccessful) {
                    val errorBody = peopleResp.errorBody()?.string() ?: "Unknown error"
                    Log.e("PROFILE_DEBUG", "People fetch failed: $errorBody")
                    withContext(Dispatchers.Main) {
                        onError("Failed to load people profile: $errorBody")
                    }
                    return@launch
                }

                val people = peopleResp.body()
                if (people == null) {
                    Log.e("PROFILE_DEBUG", "People body is null")
                    withContext(Dispatchers.Main) {
                        onError("People profile data is empty")
                    }
                    return@launch
                }

                Log.e("PROFILE_DEBUG", "People profile fetched successfully: ${people.uuid}")
                Log.e("PROFILE_DEBUG", "========================================")

                withContext(Dispatchers.Main) {
                    onSuccess(user, people)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("PROFILE_DEBUG", "Error fetching profile: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun logout(context: Context) {
        val sessionManager = SessionManager(context)
        sessionManager.clearSession()

        // Also clear registration preferences
        val userPrefs = context.getSharedPreferences("user_registration", Context.MODE_PRIVATE)
        userPrefs.edit().clear().apply()
    }
}