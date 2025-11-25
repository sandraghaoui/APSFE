package com.example.aps.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aps.R
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ConfirmBookingScreen(
    navController: NavController,
    parkingName: String,
    parkingLocation: String,
    pricePerHour: Double,
    currentCapacity: Int,
    maxCapacity: Int
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = com.example.aps.api.SessionManager(context)
    val viewModel: com.example.aps.viewmodel.ParkingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return com.example.aps.viewmodel.ParkingViewModel(sessionManager) as T
            }
        }
    )
    
    val bookingState by viewModel.bookingState.collectAsState()
    val availableSpots = maxCapacity - currentCapacity
    val rating = "4.5" // Could be fetched from DB if available
    val etaText = "15min" // Could be calculated based on location
    val distanceText = "2.3km" // Could be calculated based on location
    
    // Calculate reservation period (current time + 2 hours for demo)
    val currentTime = java.time.LocalDateTime.now()
    val checkoutTime = currentTime.plusHours(2)
    val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d, h:mm a")
    val reservationPeriod = "${currentTime.format(formatter)} - ${checkoutTime.format(formatter)}"
    val reservationTimeISO = currentTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val checkoutTimeISO = checkoutTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val totalAmount = pricePerHour * 2 // 2 hours
    
    // Colors & spacing follow your app patterns
    val pagePadding = 16.dp
    
    var isProcessing by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = pagePadding, vertical = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top bar (back + title)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.background(
                    color = Color(0xFFE2E8F0),
                    shape = CircleShape
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = Color(0xFF32403A),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = "Confirm Booking",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Review your parking details",
                    fontSize = 13.sp,
                    color = Color(0xFF8A8F94)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Map / image with badges
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            // Replace map_placeholder with your map/image drawable
            Image(
                painter = painterResource(id = R.drawable.map_placeholder),
                contentDescription = "Route map",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // ETA badge (top-right)
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_direction), // small car icon
                        contentDescription = null,
                        tint = Color(0xFF8A8F94),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(text = "ETA", fontSize = 12.sp, color = Color(0xFF8A8F94))
                        Text(text = etaText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Distance badge (bottom-left)
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_car), // small car icon
                        contentDescription = null,
                        tint = Color(0xFF8A8F94),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Distance", fontSize = 12.sp, color = Color(0xFF8A8F94))
                        Text(text = distanceText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Parking info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(text = parkingName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_location),
                                contentDescription = null,
                                tint = Color(0xFF8A8F94),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = parkingLocation, color = Color(0xFF8A8F94))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$availableSpots spots available",
                            fontSize = 13.sp,
                            color = if (availableSpots > 30) Color(0xFF22C55E) else Color(0xFFF59E0B),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = rating, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Booking details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Booking Details", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))

                // Reservation period
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null, tint = Color(0xFF8A8F94), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Reservation Period", color = Color(0xFF6E7276))
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = reservationPeriod, color = Color(0xFF8A8F94))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rate
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(painter = painterResource(id = R.drawable.ic_dollar), contentDescription = null, tint = Color(0xFF8A8F94), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Rate", color = Color(0xFF6E7276))
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "$$pricePerHour/hr", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Loading/Error states
        if (isProcessing || bookingState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF85BCA5))
            }
        }
        
        if (bookingState.error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
            ) {
                Text(
                    text = "Error: ${bookingState.error}",
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Buttons: Confirm (green) and Cancel (light)
        Button(
            onClick = {
                isProcessing = true
                val loyaltyPoints = (totalAmount * 2.5).toInt() // Earn 2.5 points per dollar
                viewModel.createReservation(
                    parkingId = parkingName,
                    time = reservationTimeISO,
                    checkoutTime = checkoutTimeISO,
                    price = totalAmount,
                    onSuccess = { reservation ->
                        isProcessing = false
                        val encodedName = URLEncoder.encode(parkingName, StandardCharsets.UTF_8.toString())
                        val encodedLocation = URLEncoder.encode(parkingLocation, StandardCharsets.UTF_8.toString())
                        navController.navigate(
                            "booking_success?parkingName=$encodedName&location=$encodedLocation&duration=2 hours&points=$loyaltyPoints"
                        ) {
                            popUpTo("user_dashboard") { inclusive = false }
                        }
                    },
                    onError = { error ->
                        isProcessing = false
                    }
                )
            },
            enabled = !isProcessing && !bookingState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF85BCA5))
        ) {
            Text(text = "Confirm Booking - $${"%.2f".format(totalAmount)}", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0))
        ) {
            Text(text = "Cancel", color = Color(0xFF6E7276), fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Keep same bottom navigation area spacing used across your app
        Spacer(modifier = Modifier.weight(1f))
    }
}
