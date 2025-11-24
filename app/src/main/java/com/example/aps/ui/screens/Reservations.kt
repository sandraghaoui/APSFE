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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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

// ---------------------------------------------
// DATA CLASS
// ---------------------------------------------
data class Reservation(
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

    val reservations = listOf(
        Reservation(
            plate = "A46123",
            status = "active",
            statusColor = Color(0xFF16C172),
            price = "$12",
            customerName = "Sandra Ghaoui",
            spot = "A-15",
            timeRange = "09:00 - 17:00"
        ),
        Reservation(
            plate = "X22789",
            status = "active",
            statusColor = Color(0xFF16C172),
            price = "$5",
            customerName = "Taylor Swift",
            spot = "B-22",
            timeRange = "10:30 - 14:30"
        ),
        Reservation(
            plate = "D15456",
            status = "upcoming",
            statusColor = Color(0xFF246BFD),
            price = "$10",
            customerName = "Lebron James",
            spot = "C-08",
            timeRange = "14:00 - 18:00"
        ),
        Reservation(
            plate = "G00789",
            status = "active",
            statusColor = Color(0xFF16C172),
            price = "$8",
            customerName = "Lionel Messi",
            spot = "A-03",
            timeRange = "11:00 - 15:00"
        )
    )

    androidx.compose.material3.Scaffold(
        bottomBar = {
            AdminReservationsBottomBar(
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

                reservations.forEach { res ->
                    ReservationCard(reservation = res)
                    Spacer(Modifier.height(12.dp))
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
fun ReservationCard(reservation: Reservation) {
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

// ---------------------------------------------
// BOTTOM NAV BAR (MATCHING ADMIN STYLE)
// ---------------------------------------------
@Composable
fun AdminReservationsBottomBar(
    activeTab: String,
    navController: NavController,
    onTabChange: (String) -> Unit
) {
    val navItems = listOf(
        Triple("dashboard", "Dashboard", Icons.Default.BarChart),
        Triple("reservations", "Reservations", Icons.Default.AccessTime),
        Triple("finances", "Finances", Icons.Default.CreditCard),
        Triple("adjustments", "Adjustments", Icons.Default.Settings)
    )

    Surface(tonalElevation = 3.dp, shadowElevation = 3.dp) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { (key, label, icon) ->
                val selected = key == activeTab
                val color =
                    if (selected) MaterialTheme.colorScheme.onSurface else Color.Gray

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            onTabChange(key)
                            when (key) {
                                "dashboard" -> navController.navigate("admin")
                                "reservations" -> navController.navigate("admin_reservations")
                                "finances" -> navController.navigate("financial_reports")
                                "adjustments" -> navController.navigate("admin_settings")
                            }
                        }
                        .padding(4.dp)
                ) {
                    androidx.compose.material3.Icon(
                        icon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(label, color = color, fontSize = 12.sp)
                }
            }
        }
    }
}

// PREVIEW (optional)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAdminReservations() {
    AdminReservationsScreen(rememberNavController())
}
