package com.example.aps.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aps.R
import com.example.aps.api.ParkingRead
import com.example.aps.api.SessionManager
import com.example.aps.viewmodel.ParkingViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * UserDashboard - Main screen for regular users after login
 * Shows available parking spots from DB with real-time data
 */
@Composable
fun UserDashboard(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val viewModel: ParkingViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ParkingViewModel(sessionManager) as T
            }
        }
    )
    
    val uiState by viewModel.parkingState.collectAsState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    
    val greenLight = Color(0xFF85BCA5)
    val greenTop = Color(0xFF354E44)
    val greenBottom = Color(0xFF263931)
    val bottomBarHeight = 70.dp
    
    // Responsive dimensions
    val horizontalPadding = (screenWidth * 0.04f).coerceAtMost(16.dp).coerceAtLeast(12.dp)
    val verticalPadding = (screenHeight * 0.02f).coerceAtMost(24.dp).coerceAtLeast(16.dp)
    val headerHeight = (screenHeight * 0.25f).coerceAtMost(200.dp).coerceAtLeast(160.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomBarHeight)
        ) {
            // ---------------- HEADER ----------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.home_header),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    greenLight.copy(alpha = 0.65f),
                                    greenTop.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size((headerHeight * 0.25f).coerceAtMost(60.dp))
                            .background(Color(0xFFF6F7F8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.apslogo),
                            contentDescription = "APS logo",
                            modifier = Modifier.size((headerHeight * 0.2f).coerceAtMost(40.dp))
                        )
                    }

                    Text(
                        text = "Welcome to APS",
                        color = Color.White,
                        fontSize = (screenWidth.value * 0.065f).coerceIn(20f, 26f).sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Automated Parking System",
                        color = Color(0xFFE5F4EF),
                        fontSize = (screenWidth.value * 0.035f).coerceIn(12f, 14f).sp
                    )
                }
            }

            Spacer(Modifier.height(verticalPadding))

            // ---------------- SUBTITLE ----------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding * 1.5f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Available Parking Spots",
                    fontSize = (screenWidth.value * 0.05f).coerceIn(18f, 22f).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Choose from ${uiState.parkings.size} available locations",
                    fontSize = (screenWidth.value * 0.032f).coerceIn(11f, 13f).sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(verticalPadding * 1.5f))

            // ---------------- LOADING / ERROR / PARKING LIST ----------------
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = greenLight)
                    }
                }
                uiState.error != null -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error Loading Parkings",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = uiState.error ?: "Unknown error",
                                fontSize = 13.sp,
                                color = Color(0xFF991B1B)
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadParkings() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                uiState.parkings.isEmpty() -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Parking Spots Available",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Check back later for availability",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                else -> {
                    // Display parking cards
                    Column(
                        modifier = Modifier.padding(horizontal = horizontalPadding),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        uiState.parkings.forEach { parking ->
                            ParkingCardItem(
                                parking = parking,
                                onClick = {
                                    // Encode parking data for navigation
                                    val encodedName = URLEncoder.encode(parking.name, StandardCharsets.UTF_8.toString())
                                    val encodedLocation = URLEncoder.encode(parking.location, StandardCharsets.UTF_8.toString())
                                    val encodedOpenTime = URLEncoder.encode(parking.open_time, StandardCharsets.UTF_8.toString())
                                    val encodedCloseTime = URLEncoder.encode(parking.close_time, StandardCharsets.UTF_8.toString())
                                    navController.navigate(
                                        "confirm_booking?parkingName=$encodedName&location=$encodedLocation&pricePerHour=${parking.price_per_hour}&currentCapacity=${parking.current_capacity}&maxCapacity=${parking.maximum_capacity}&openTime=$encodedOpenTime&closeTime=$encodedCloseTime"
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(verticalPadding * 2f))
        }

        // ---------------- BOTTOM NAV ----------------
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomBarHeight)
                .background(Color.White)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Home",
                imageRes = R.drawable.ic_nav_home,
                selected = true,
                onClick = { navController.navigate("user_dashboard") }
            )
            BottomNavItem(
                label = "Payments",
                imageRes = R.drawable.ic_nav_payments,
                onClick = { navController.navigate("purchase_history") }
            )
            BottomNavItem(
                label = "Loyalty",
                imageRes = R.drawable.ic_nav_loyalty,
                onClick = { navController.navigate("loyalty") }
            )
            BottomNavItem(
                label = "Profile",
                imageRes = R.drawable.ic_nav_profile,
                onClick = { navController.navigate("profile") }
            )
        }
    }
}

@Composable
fun ParkingCardItem(
    parking: ParkingRead,
    onClick: () -> Unit
) {
    val availableSpots = parking.maximum_capacity - parking.current_capacity
    val occupancyPercent = ((parking.current_capacity.toDouble() / parking.maximum_capacity) * 100).toInt()
    val statusColor = if (availableSpots > 30) Color(0xFF22C55E) else Color(0xFFF59E0B)
    val barColor = if (occupancyPercent < 70) Color(0xFF22C55E) else Color(0xFFF59E0B)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = parking.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(Modifier.height(6.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location),
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = parking.location,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$${parking.price_per_hour}/hr",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = "$availableSpots spots left",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }
                }
                
                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (availableSpots > 30) "Available" else "Limited",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Occupancy bar
            Column {
                Text(
                    text = "Occupancy: $occupancyPercent%",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE5E7EB))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(occupancyPercent / 100f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(barColor)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF85BCA5))
            ) {
                Text(
                    text = "Book Now",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    label: String,
    imageRes: Int,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val greenColor = Color(0xFF85BCA5)
    val backgroundColor = if (selected) greenColor.copy(alpha = 0.15f) else Color.Transparent
    
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = label,
                modifier = Modifier
                    .size(22.dp)
                    .alpha(if (selected) 1f else 0.5f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) greenColor else Color.Gray
            )
        }
    }
}


