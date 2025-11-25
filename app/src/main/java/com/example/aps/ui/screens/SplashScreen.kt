package com.example.aps.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.aps.R
import com.example.aps.api.ApiService
import com.example.aps.api.RetrofitClient
import com.example.aps.api.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    
    LaunchedEffect(Unit) {
        // Minimum splash screen display time
        delay(1500)
        
        // Check if user has a stored token
        val token = sessionManager.getAccessToken()
        
        if (token != null && token.isNotBlank()) {
            // Token exists, verify it's still valid by making an API call
            try {
                val api = RetrofitClient.getClient { sessionManager.getAccessToken() }
                    .create(ApiService::class.java)
                
                // Try to get user profile - this will fail if token is invalid
                val userResp = api.getMyProfile()
                
                if (userResp.isSuccessful && userResp.body() != null) {
                    val user = userResp.body()!!
                    val isAdmin = user.admin
                    
                    // Token is valid, navigate to appropriate dashboard
                    withContext(Dispatchers.Main) {
                        if (isAdmin) {
                            navController.navigate("admin") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            navController.navigate("user_dashboard") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }
                    return@LaunchedEffect
                } else {
                    // API call failed (401/403), token is invalid
                    sessionManager.clearSession()
                }
            } catch (e: Exception) {
                // API call failed, token might be invalid or expired
                // Clear session and go to login
                sessionManager.clearSession()
            }
        }
        
        // No valid session or token, go to home/login screen
        withContext(Dispatchers.Main) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = "Automated Parking System",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Image(
                painter = painterResource(id = R.drawable.apslogo),
                contentDescription = "Automated Parking System Logo",
                modifier = Modifier
                    .width(180.dp)
                    .height(400.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Here we Park!",
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
