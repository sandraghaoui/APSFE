package com.example.aps.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.unit.sp
import com.example.aps.api.ApiService
import com.example.aps.api.ParkingRead
import com.example.aps.api.RetrofitClient
import com.example.aps.api.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * SETTINGS CARD
 */
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

/**
 * EDIT BUTTON
 */
@Composable
fun EditButton(onClick: () -> Unit = {}) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF3CA3FF))
    ) {
        Text("EDIT", fontWeight = FontWeight.Medium)
        Icon(
            Icons.Default.Edit,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}

// Old AdminSettingsBottomBar removed - using shared AdminBottomNavBar

/**
 * MAIN SCREEN
 */
@Composable
fun AdminSettingsScreen(navController: NavController) {

    var activeTab by remember { mutableStateOf("adjustments") }
    
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val api = remember {
        RetrofitClient.getClient { sessionManager.getAccessToken() }
            .create(ApiService::class.java)
    }
    
    var parking by remember { mutableStateOf<ParkingRead?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val adminResp = api.getMyAdmin()
                if (adminResp.isSuccessful && adminResp.body() != null) {
                    val adminUuid = adminResp.body()!!.uuid
                    val parkingResp = api.listParkings()
                    if (parkingResp.isSuccessful && parkingResp.body() != null) {
                        val allParkings = parkingResp.body()!!
                        parking = allParkings.firstOrNull { it.owner_uuid == adminUuid }
                            ?: allParkings.firstOrNull()
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    Scaffold(
        bottomBar = {
            AdminBottomNavBar(
                activeTab = activeTab,
                navController = navController,
                onTabChange = { activeTab = it }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {

            // HEADER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Text(
                    "Admin Settings",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        parking?.name ?: "Loading...",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date()),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // CONTENT
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Parking Spots
                Text("Parking Spots", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Current Occupancy", color = Color.Gray)
                            Spacer(Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (parking != null) "${parking!!.current_capacity}/${parking!!.maximum_capacity}"
                                    else "Loading...",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(12.dp))

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.DirectionsCar, null, tint = Color.Gray)
                                    }
                                }
                            }

                            Text(
                                if (parking != null && parking!!.maximum_capacity > 0) {
                                    "${((parking!!.current_capacity.toFloat() / parking!!.maximum_capacity) * 100).toInt()}% full"
                                } else "N/A",
                                color = Color(0xFF22C55E)
                            )
                        }
                        EditButton()
                    }
                }

                // Pricing Adjustments
                Text("Pricing Adjustments", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Price/Hour", color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (parking != null) String.format("$%.2f", parking!!.price_per_hour)
                                    else "Loading...",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(12.dp))

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.AttachMoney, null, tint = Color.Gray)
                                    }
                                }
                            }
                        }
                        EditButton()
                    }
                }

                // ⭐⭐⭐ LOYALTY SETTINGS (RESTORED)
                Text("Loyalty Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                SettingsCard {
                    Text(
                        "You can activate loyalty rewards for your customers to benefit from choosing you.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Learn more",
                        color = Color(0xFF3CA3FF),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3CD6B7),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Activate", fontWeight = FontWeight.Bold)
                    }
                }

                // ---- LOGOUT BUTTON ----
                Button(
                    onClick = {
                        sessionManager.clearSession()
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF85BCA5)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF85BCA5))
                ) {
                    Text("Logout", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}
