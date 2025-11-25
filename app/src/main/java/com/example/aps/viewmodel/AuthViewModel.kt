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
    // REGISTER USER - ORIGINAL
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
                Log.e("REGISTER_DEBUG", "Starting registration for: $email")

                val userEmail = email.trim()
                val userPassword = password.trim()

                // Store pending data FIRST
                val userPrefs = context.getSharedPreferences("user_registration", Context.MODE_PRIVATE)
                userPrefs.edit().apply {
                    putString("pending_fullName", fullName.trim())
                    putString("pending_phone", phone.trim())
                    putString("pending_plate", plate.trim())
                    putString("pending_email", userEmail)
                    putBoolean("profile_created", false)
                    apply()
                }

                Log.e("REGISTER_DEBUG", "Stored pending data: $fullName, $phone, $plate")

                // Supabase signup
                val supabaseAuth = SupabaseClientProvider.client.auth
                supabaseAuth.signUpWith(Email) {
                    this.email = userEmail
                    this.password = userPassword
                }

                Log.e("REGISTER_DEBUG", "Supabase signup completed")

                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                Log.e("REGISTER_DEBUG", "Registration error: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Registration failed: ${e.message}")
                }
            }
        }
    }

    // ============================================================
    // LOGIN USER - ROLLED BACK + SMALL FIX
    // ============================================================
    fun loginUser(
        context: Context,
        email: String,
        password: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.e("LOGIN_DEBUG", "========================================")
        Log.e("LOGIN_DEBUG", "Starting login for: $email")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sessionManager = SessionManager(context)
                val supabaseAuth = SupabaseClientProvider.client.auth

                val userEmail = email.trim()
                val userPassword = password.trim()

                // 1) SUPABASE LOGIN
                Log.e("LOGIN_DEBUG", "Attempting Supabase login...")
                supabaseAuth.signInWith(Email) {
                    this.email = userEmail
                    this.password = userPassword
                }

                val session = supabaseAuth.currentSessionOrNull()
                val token = session?.accessToken
                val uuid = session?.user?.id

                Log.e("LOGIN_DEBUG", "Session: ${session != null}, Token: ${token != null}, UUID: $uuid")

                if (session == null || token == null || uuid == null) {
                    withContext(Dispatchers.Main) {
                        onError("Login failed: Invalid session")
                    }
                    return@launch
                }

                // Save token
                sessionManager.saveAccessToken(token)
                Log.e("LOGIN_DEBUG", "Token saved")

                // 2) Create API client
                val api = RetrofitClient.getClient { sessionManager.getAccessToken() }
                    .create(ApiService::class.java)

                // 3) Check profile_created flag in SharedPreferences
                val userPrefs = context.getSharedPreferences("user_registration", Context.MODE_PRIVATE)
                var profileCreated = userPrefs.getBoolean("profile_created", false)

                Log.e("LOGIN_DEBUG", "Profile created flag (before server check): $profileCreated")

                // ------------------------------------------------------------
                // A) If profileCreated == false, first check if backend profile
                //    already exists. If yes, mark flag true and skip bootstrap.
                // ------------------------------------------------------------
                if (!profileCreated) {
                    Log.e("LOGIN_DEBUG", "Checking /users/me on server...")

                    try {
                        val existingProfileResp = api.getMyProfile()

                        if (existingProfileResp.isSuccessful && existingProfileResp.body() != null) {
                            val user = existingProfileResp.body()!!
                            val isAdmin = user.admin

                            // Backend already has profile -> mark as created locally
                            userPrefs.edit().putBoolean("profile_created", true).apply()
                            profileCreated = true

                            Log.e("LOGIN_DEBUG", "Profile already exists on server. Skipping first-login bootstrap.")
                            Log.e("LOGIN_DEBUG", "User: ${user.first_name} ${user.last_name}")
                            Log.e("LOGIN_DEBUG", "Admin: $isAdmin")
                            Log.e("LOGIN_DEBUG", "========================================")

                            withContext(Dispatchers.Main) {
                                onSuccess(isAdmin)
                            }
                            return@launch
                        } else if (!existingProfileResp.isSuccessful && existingProfileResp.code() != 404) {
                            // Some other server error
                            Log.e("LOGIN_DEBUG", "Error checking profile: ${existingProfileResp.code()}")
                            withContext(Dispatchers.Main) {
                                onError("Failed to load profile: ${existingProfileResp.code()}")
                            }
                            return@launch
                        }

                        // If 404 → no profile on backend. We'll fall through to original
                        // first-login bootstrap using pending registration data.
                        Log.e("LOGIN_DEBUG", "No existing profile on server (404). Will run first-login bootstrap.")

                    } catch (e: Exception) {
                        Log.e("LOGIN_DEBUG", "Exception while checking existing profile: ${e.message}")
                        // If this fails, we fall back to original behavior
                    }
                }

                // ------------------------------------------------------------
                // B) ORIGINAL FIRST-LOGIN FLOW (only if profileCreated==false)
                // ------------------------------------------------------------
                if (!profileCreated) {
                    Log.e("LOGIN_DEBUG", "=== FIRST LOGIN - CREATING PROFILES ===")

                    // Get pending registration data
                    val pendingName = userPrefs.getString("pending_fullName", "") ?: ""
                    val pendingPhone = userPrefs.getString("pending_phone", "") ?: ""
                    val pendingPlate = userPrefs.getString("pending_plate", "0") ?: "0"
                    val pendingEmail = userPrefs.getString("pending_email", "") ?: userEmail

                    Log.e(
                        "LOGIN_DEBUG",
                        "Pending data: Name='$pendingName', Phone='$pendingPhone', Plate='$pendingPlate'"
                    )

                    if (pendingName.isEmpty()) {
                        Log.e("LOGIN_DEBUG", "ERROR: No pending registration data found")
                        withContext(Dispatchers.Main) {
                            onError("Registration data not found. Please sign up again.")
                        }
                        return@launch
                    }

                    // Parse name
                    val parts = pendingName.split(" ", limit = 2)
                    val firstName = parts.firstOrNull() ?: ""
                    val lastName = parts.getOrNull(1) ?: ""

                    Log.e("LOGIN_DEBUG", "Creating user profile: $firstName $lastName")

                    // Create User Profile
                    try {
                        val userResp = api.createOrUpdateMyProfile(
                            UserCreate(
                                first_name = firstName,
                                last_name = lastName,
                                phone_number = pendingPhone,
                                email = pendingEmail,
                                admin = false
                            )
                        )

                        Log.e("LOGIN_DEBUG", "User API response: ${userResp.code()}")

                        if (!userResp.isSuccessful) {
                            val errorBody = userResp.errorBody()?.string()
                            Log.e("LOGIN_DEBUG", "User profile error: $errorBody")

                            // If 409, profile already exists, that's OK
                            if (userResp.code() != 409) {
                                withContext(Dispatchers.Main) {
                                    onError("Failed to create user profile: ${userResp.code()}")
                                }
                                return@launch
                            } else {
                                Log.e("LOGIN_DEBUG", "User profile already exists (409), continuing...")
                            }
                        } else {
                            Log.e("LOGIN_DEBUG", "✓ User profile created successfully")
                        }

                    } catch (e: Exception) {
                        Log.e("LOGIN_DEBUG", "Exception creating user profile: ${e.message}")
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            onError("Failed to create user profile: ${e.message}")
                        }
                        return@launch
                    }

                    // Create People Profile
                    try {
                        val plateNumber = pendingPlate.toIntOrNull() ?: 0
                        Log.e("LOGIN_DEBUG", "Creating people profile with plate: $plateNumber")

                        val peopleResp = api.createPeople(
                            PeopleCreate(
                                plate_number = plateNumber,
                                loyalty_points = 0,
                                balance = 0.0
                            )
                        )

                        Log.e("LOGIN_DEBUG", "People API response: ${peopleResp.code()}")

                        if (!peopleResp.isSuccessful) {
                            val errorBody = peopleResp.errorBody()?.string()
                            Log.e("LOGIN_DEBUG", "People profile error: $errorBody")

                            // If 409, profile already exists, that's OK
                            if (peopleResp.code() != 409) {
                                withContext(Dispatchers.Main) {
                                    onError("Failed to create people profile: ${peopleResp.code()}")
                                }
                                return@launch
                            } else {
                                Log.e("LOGIN_DEBUG", "People profile already exists (409), continuing...")
                            }
                        } else {
                            Log.e("LOGIN_DEBUG", "✓ People profile created successfully")
                        }

                    } catch (e: Exception) {
                        Log.e("LOGIN_DEBUG", "Exception creating people profile: ${e.message}")
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            onError("Failed to create people profile: ${e.message}")
                        }
                        return@launch
                    }

                    // Mark profile as created
                    userPrefs.edit().putBoolean("profile_created", true).apply()
                    Log.e("LOGIN_DEBUG", "✓ Profile created flag set to true")
                }

                // ------------------------------------------------------------
                // C) STANDARD FLOW: fetch profile, return isAdmin
                // ------------------------------------------------------------
                Log.e("LOGIN_DEBUG", "Fetching user profile for admin check...")

                val userResp = api.getMyProfile()
                if (!userResp.isSuccessful || userResp.body() == null) {
                    Log.e("LOGIN_DEBUG", "Failed to fetch profile: ${userResp.code()}")
                    withContext(Dispatchers.Main) {
                        onError("Failed to load profile: ${userResp.code()}")
                    }
                    return@launch
                }

                val user = userResp.body()!!
                val isAdmin = user.admin

                Log.e("LOGIN_DEBUG", "✓✓✓ LOGIN SUCCESSFUL ✓✓✓")
                Log.e("LOGIN_DEBUG", "User: ${user.first_name} ${user.last_name}")
                Log.e("LOGIN_DEBUG", "Admin: $isAdmin")
                Log.e("LOGIN_DEBUG", "========================================")

                withContext(Dispatchers.Main) {
                    onSuccess(isAdmin)
                }

            } catch (e: Exception) {
                Log.e("LOGIN_DEBUG", "LOGIN EXCEPTION: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Login failed: ${e.message}")
                }
            }
        }
    }

    // ============================================================
    // PROFILE FETCH - ORIGINAL
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

    // ============================================================
    // LOGOUT - ORIGINAL
    // ============================================================
    fun logout(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Clear Supabase session
                SupabaseClientProvider.client.auth.signOut()
            } catch (e: Exception) {
                // Ignore errors during logout
            }
        }
        SessionManager(context).clearSession()
        context.getSharedPreferences("user_registration", Context.MODE_PRIVATE).edit().clear().apply()
    }
}
