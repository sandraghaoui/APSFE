package com.example.aps.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aps.R

// ---------------------------------------------------------
// DATA CLASS FOR PARKING
// ---------------------------------------------------------
data class Parking(
    val title: String,
    val address: String,
    val available: Int,
    val total: Int,
    val price: Double,
    val rating: Double,
    val distanceMeters: Int,
    val tags: List<String>
)

// ---------------------------------------------------------
// PARKING CARD
// ---------------------------------------------------------
@Composable
fun ParkingCard(
    p: Parking
) {
    val statusColor = if (p.available > 30) Color(0xFF22C55E) else Color(0xFFE53935)
    val occupancyPercent = ((p.available.toDouble() / p.total) * 100).toInt()
    val barColor = if (occupancyPercent < 50) Color(0xFF22C55E) else Color(0xFFE53935)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                // LEFT BADGE
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("${p.available}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = statusColor)
                    Text("of ${p.total}", color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (p.available > 30) "Available" else "Almost Full",
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(p.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(p.address, color = Color.Gray, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$${p.price}/hr", color = Color(0xFF22C55E), fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${p.rating}", fontSize = 13.sp)
                        Spacer(Modifier.width(10.dp))
                        Text("${p.distanceMeters}m", fontSize = 13.sp, color = Color.Gray)
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        p.tags.forEach {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE5E7EB))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(it, fontSize = 11.sp, color = Color.DarkGray)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text("Occupancy", fontSize = 13.sp, color = Color.Gray)

                    Spacer(Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE5E7EB))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(p.available.toFloat() / p.total)
                                .height(6.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(barColor)
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Text("${occupancyPercent}% full", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2341C1))
            ) {
                Text("Book Now", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

// ---------------------------------------------------------
// MAIN SCREEN WITH FILTERING
// ---------------------------------------------------------
@Composable
fun FindParkingScreen(navController: NavController) {

    val bottomBarHeight = 60.dp

    var selectedFilter by remember { mutableStateOf("Nearest") }

    val allParkings = listOf(
        Parking("OSB Parking Raouche", "123 Main Street", 87, 150, 5.0, 4.5, 700, listOf("24/7", "Covered", "Valet Parking")),
        Parking("Bassam’s Parking", "Near Cheese on top", 12, 200, 4.0, 4.3, 800, listOf("Indoor", "Valet Available")),
        Parking("AUBMC Parking", "Facing the hospital", 245, 300, 6.0, 4.7, 400, listOf("24/7", "Security"))
    )

    val filtered = when (selectedFilter) {
        "Cheapest" -> allParkings.sortedBy { it.price }
        "Best" -> allParkings.sortedBy { it.distanceMeters } // closest
        "Available" -> allParkings.filter { it.available > 0 }
        else -> allParkings.sortedBy { it.distanceMeters } // default = nearest
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomBarHeight)
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(16.dp))

            Text("Find Your Parking", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            // FILTER BUTTONS
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                listOf("Nearest", "Cheapest", "Best", "Available").forEach { label ->
                    Text(
                        label,
                        color = if (selectedFilter == label) Color(0xFF85BCA5) else Color.Gray,
                        fontWeight = if (selectedFilter == label) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.clickable { selectedFilter = label }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // LIST OF FILTERED PARKINGS
            filtered.forEach {
                ParkingCard(it)
                Spacer(Modifier.height(20.dp))
            }

            Spacer(Modifier.height(40.dp))
        }

        // Bottom Nav (same as before)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomBarHeight)
                .background(Color.White)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem("Home", R.drawable.ic_nav_home, true) { navController.navigate("home") }
            BottomNavItem("Payments", R.drawable.ic_nav_payments) { navController.navigate("purchase_history") }
            BottomNavItem("Loyalty", R.drawable.ic_nav_loyalty) { navController.navigate("loyalty") }
            BottomNavItem("Profile", R.drawable.ic_nav_profile) { navController.navigate("profile") }
        }
    }
}

// ---------------------------------------------------------
// PREVIEW
// ---------------------------------------------------------
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewFindParking() {
    FindParkingScreen(navController = rememberNavController())
}
