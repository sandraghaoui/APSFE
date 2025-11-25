package com.example.aps.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.aps.api.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ---------------------------------------------
// DATA CLASS
// ---------------------------------------------
data class ReservationDisplay(
    val plate: String,
    val status: String,
    val statusColor: Color,
    val price: String,
    val customerName: String,
    val spot: String,
    val timeRange: String
)

// ---------------------------------------------
// MAIN SCREEN
// ---------------------------------------------
@Composable
fun AdminReservationsScreen(navController: NavController) {

    var activeTab by remember { mutableStateOf("reservations") }

    val background = Color(0xFFF6F7F8)
    
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val api = remember {
        RetrofitClient.getClient { sessionManager.getAccessToken() }
            .create(ApiService::class.java)
    }
    
    var reservations by remember { mutableStateOf<List<ReservationDisplay>>(emptyList()) }
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
                        val parking = allParkings.firstOrNull { it.owner_uuid == adminUuid }
                            ?: allParkings.firstOrNull()
                        
                        if (parking != null) {
                            val reservationsResp = api.listReservations()
                            if (reservationsResp.isSuccessful && reservationsResp.body() != null) {
                                val allReservations = reservationsResp.body()!!
                                val parkingReservations = allReservations.filter { it.parking_id == parking.name }
                                
                                // Get all people to map UUIDs to names
                                val peopleResp = api.listPeople()
                                val peopleMap = if (peopleResp.isSuccessful && peopleResp.body() != null) {
                                    peopleResp.body()!!.associateBy { it.uuid }
                                } else emptyMap()
                                
                                // Get all users to get names
                                val usersMap = mutableMapOf<String, String>()
                                // Note: We don't have a direct API to get user by UUID, so we'll use plate number as identifier
                                
                                reservations = parkingReservations.map { res ->
                                    val person = peopleMap[res.people_uuid]
                                    val plateNumber = person?.plate_number?.toString() ?: "N/A"
                                    
                                    // Parse time
                                    val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                    val checkoutFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                    val displayFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    
                                    val startTime = try {
                                        timeFormat.parse(res.time) ?: Date()
                                    } catch (e: Exception) { Date() }
                                    
                                    val endTime = if (res.checkout_time != null) {
                                        try {
                                            checkoutFormat.parse(res.checkout_time) ?: Date()
                                        } catch (e: Exception) { Date() }
                                    } else {
                                        val cal = Calendar.getInstance()
                                        cal.time = startTime
                                        cal.add(Calendar.HOUR, 2) // Default 2 hours if no checkout
                                        cal.time
                                    }
                                    
                                    val timeRange = "${displayFormat.format(startTime)} - ${displayFormat.format(endTime)}"
                                    
                                    val statusColor = when (res.status.lowercase()) {
                                        "pending", "active" -> Color(0xFF16C172)
                                        "upcoming" -> Color(0xFF246BFD)
                                        else -> Color.Gray
                                    }
                                    
                                    ReservationDisplay(
                                        plate = plateNumber,
                                        status = res.status.lowercase(),
                                        statusColor = statusColor,
                                        price = String.format("$%.2f", res.price),
                                        customerName = "Customer ${plateNumber}", // Using plate as identifier
                                        spot = "Spot ${res.id}", // Using reservation ID as spot
                                        timeRange = timeRange
                                    )
                                }
                            }
                        }
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    androidx.compose.material3.Scaffold(
        bottomBar = {
            AdminBottomNavBar(
                activeTab = activeTab,
                navController = navController,
                onTabChange = { activeTab = it }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Active Reservations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else if (reservations.isEmpty()) {
                    Text("No reservations found", color = Color.Gray)
                } else {
                    reservations.forEach { res ->
                        ReservationCard(reservation = res)
                        Spacer(Modifier.height(12.dp))
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

// ---------------------------------------------
// RESERVATION CARD
// ---------------------------------------------
@Composable
fun ReservationCard(reservation: ReservationDisplay) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP PART
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(Color(0xFFF2F5F7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🚗", fontSize = 16.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = reservation.plate,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(reservation.statusColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = reservation.status,
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 2.dp
                                )
                            )
                        }
                    }

                    Text(
                        text = reservation.price,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = reservation.customerName,
                    fontSize = 17.sp
                )
            }

            // BOTTOM ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spot: ${reservation.spot}",
                    fontSize = 15.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏱️", fontSize = 15.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = reservation.timeRange,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// Old AdminReservationsBottomBar removed - using shared AdminBottomNavBar

// PREVIEW (optional)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAdminReservations() {
    AdminReservationsScreen(rememberNavController())
}
