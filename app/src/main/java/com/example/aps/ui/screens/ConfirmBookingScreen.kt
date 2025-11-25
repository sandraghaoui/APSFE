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
    maxCapacity: Int,
    openTime: String,
    closeTime: String
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
    
    // Decode parking name in case it has URL-encoded characters (e.g., %27 for apostrophe)
    val decodedParkingName = java.net.URLDecoder.decode(parkingName, "UTF-8")
    val decodedParkingLocation = java.net.URLDecoder.decode(parkingLocation, "UTF-8")
    val decodedOpenTime = java.net.URLDecoder.decode(openTime, "UTF-8")
    val decodedCloseTime = java.net.URLDecoder.decode(closeTime, "UTF-8")
    
    // Parse open/close times for DAILY hours validation
    val currentTime = java.time.LocalTime.now()
    val openTime24h = try {
        // Parse the datetime string and extract just the time portion
        val parsed = java.time.LocalDateTime.parse(decodedOpenTime.replace(" ", "T"))
        parsed.toLocalTime()
    } catch (e: Exception) {
        android.util.Log.e("ConfirmBookingScreen", "Failed to parse open time: ${e.message}")
        null
    }
    val closeTime24h = try {
        val parsed = java.time.LocalDateTime.parse(decodedCloseTime.replace(" ", "T"))
        parsed.toLocalTime()
    } catch (e: Exception) {
        android.util.Log.e("ConfirmBookingScreen", "Failed to parse close time: ${e.message}")
        null
    }
    
    // Check if parking is currently open (daily hours)
    val isParkingOpen = if (openTime24h != null && closeTime24h != null) {
        if (closeTime24h.isAfter(openTime24h)) {
            // Normal case: opens and closes on same day (e.g., 8 AM - 10 PM)
            currentTime.isAfter(openTime24h) && currentTime.isBefore(closeTime24h)
        } else {
            // Spans midnight: opens before midnight, closes after (e.g., 10 PM - 2 AM)
            currentTime.isAfter(openTime24h) || currentTime.isBefore(closeTime24h)
        }
    } else {
        true // If we can't parse times, allow booking (backend will validate)
    }
    
    // Check if parking is full
    val availableSpots = maxCapacity - currentCapacity
    val isParkingFull = availableSpots <= 0
    
    // Determine warning message
    val warningMessage = when {
        isParkingFull -> "This parking is currently full. No spots available."
        !isParkingOpen && openTime24h != null && closeTime24h != null -> {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a")
            "This parking is closed. Opens daily at ${openTime24h.format(formatter)}, closes at ${closeTime24h.format(formatter)}."
        }
        else -> null
    }
    
    val canBook = isParkingOpen && !isParkingFull
    
    // Log received parameters
    android.util.Log.d("ConfirmBookingScreen", "=== Screen Parameters ===")
    android.util.Log.d("ConfirmBookingScreen", "Parking Name (decoded): $decodedParkingName")
    android.util.Log.d("ConfirmBookingScreen", "Location: $decodedParkingLocation")
    android.util.Log.d("ConfirmBookingScreen", "Open Time (24h): $openTime24h")
    android.util.Log.d("ConfirmBookingScreen", "Close Time (24h): $closeTime24h")
    android.util.Log.d("ConfirmBookingScreen", "Current Time: $currentTime")
    android.util.Log.d("ConfirmBookingScreen", "Is Open: $isParkingOpen")
    android.util.Log.d("ConfirmBookingScreen", "Available Spots: $availableSpots")
    android.util.Log.d("ConfirmBookingScreen", "Is Full: $isParkingFull")
    android.util.Log.d("ConfirmBookingScreen", "Can Book: $canBook")
    android.util.Log.d("ConfirmBookingScreen", "Warning Message: $warningMessage")
    
    val bookingState by viewModel.bookingState.collectAsState()
    var showWarningCard by remember { mutableStateOf(false) }
    val rating = "4.5" // Could be fetched from DB if available
    val etaText = "15min" // Could be calculated based on location
    val distanceText = "2.3km" // Could be calculated based on location
    
    // Calculate reservation period (current time + 2 hours for demo)
    val reservationStartTime = java.time.ZonedDateTime.now()
    val checkoutTime = reservationStartTime.plusHours(2)
    val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d, h:mm a")
    val reservationPeriod = "${reservationStartTime.format(formatter)} - ${checkoutTime.format(formatter)}"
    // Supabase expects ISO 8601 format with timezone (e.g., "2025-11-25T14:30:00+00:00")
    val reservationTimeISO = reservationStartTime.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    val checkoutTimeISO = checkoutTime.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    val totalAmount = pricePerHour * 2 // 2 hours
    
    android.util.Log.d("ConfirmBookingScreen", "Reservation Time ISO: $reservationTimeISO")
    android.util.Log.d("ConfirmBookingScreen", "Checkout Time ISO: $checkoutTimeISO")
    android.util.Log.d("ConfirmBookingScreen", "Total Amount: $totalAmount")
    
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
                        Text(text = decodedParkingName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_location),
                                contentDescription = null,
                                tint = Color(0xFF8A8F94),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = decodedParkingLocation, color = Color(0xFF8A8F94))
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

        // Warning card for closed or full parking
        if (showWarningCard && warningMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = warningMessage,
                        color = Color(0xFF92400E),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

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
            enabled = canBook && !isProcessing,
            onClick = {
                if (!canBook) {
                    showWarningCard = true
                    return@Button
                }
                isProcessing = true
                val loyaltyPoints = (totalAmount * 2.5).toInt() // Earn 2.5 points per dollar
                viewModel.createReservation(
                    parkingId = decodedParkingName,
                    time = reservationTimeISO,
                    checkoutTime = checkoutTimeISO,
                    price = totalAmount,
                    onSuccess = { reservation ->
                        android.util.Log.d("ConfirmBookingScreen", "")
                        android.util.Log.d("ConfirmBookingScreen", "========================================")
                        android.util.Log.d("ConfirmBookingScreen", "📋 RESERVATION SUCCESSFUL - STARTING UPDATES")
                        android.util.Log.d("ConfirmBookingScreen", "========================================")
                        
                        // Update parking capacity (increment by 1)
                        val newCapacity = currentCapacity + 1
                        android.util.Log.d("ConfirmBookingScreen", "Starting capacity update: $currentCapacity -> $newCapacity")
                        
                        viewModel.updateParkingCapacity(decodedParkingName, newCapacity) {
                            android.util.Log.d("ConfirmBookingScreen", "✅ Capacity update completed, starting loyalty points update...")
                            
                            // Update loyalty points after capacity update completes
                            viewModel.updateLoyaltyPoints(loyaltyPoints) {
                                android.util.Log.d("ConfirmBookingScreen", "✅ Loyalty points update completed")
                                android.util.Log.d("ConfirmBookingScreen", "Refreshing parking list...")
                                
                                // Reload parking list to reflect updated capacity
                                viewModel.loadParkings()
                                
                                android.util.Log.d("ConfirmBookingScreen", "Navigating to success screen...")
                                isProcessing = false
                                val encodedName = URLEncoder.encode(decodedParkingName, StandardCharsets.UTF_8.toString())
                                val encodedLocation = URLEncoder.encode(decodedParkingLocation, StandardCharsets.UTF_8.toString())
                                navController.navigate(
                                    "booking_success?parkingName=$encodedName&location=$encodedLocation&duration=2 hours&points=$loyaltyPoints"
                                ) {
                                    popUpTo("user_dashboard") { inclusive = false }
                                }
                                android.util.Log.d("ConfirmBookingScreen", "========================================")
                                android.util.Log.d("ConfirmBookingScreen", "")
                            }
                        }
                    },
                    onError = { error ->
                        isProcessing = false
                    }
                )
            },
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
