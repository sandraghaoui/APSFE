package com.example.aps.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aps.R

// ----------------------------------------------------------------------
//  ADMIN DASHBOARD MAIN SCREEN
// ----------------------------------------------------------------------
@Composable
fun AdminDashboard(navController: NavController) {

    var activeTab by remember { mutableStateOf("dashboard") }

    Scaffold(
        bottomBar = {
            AdminDashboardBottomBar(
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
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF8FAFC))
                .padding(16.dp)
        ) {

            // HEADER -------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Samer's Parking",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Sunday, September 28, 2025",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(22.dp))

            // STATS GRID ---------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Current Occupancy",
                        value = "247/300",
                        change = "+12 from yesterday",
                        iconRes = R.drawable.ic_car,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Active Reservations",
                        value = "45",
                        change = "+5 today",
                        iconRes = R.drawable.ic_calendar,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Daily Revenue",
                        value = "$2,450",
                        change = "+8.5%",
                        iconRes = R.drawable.ic_revenue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Customers",
                        value = "892",
                        change = "+23 this week",
                        iconRes = R.drawable.ic_users,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // CHART --------------------------------------------------------------
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(Color.White)
            ) {
                Column(Modifier.padding(18.dp)) {

                    Text(
                        "Today's Occupancy",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(40.dp))

                    OccupancyChart()
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

// ----------------------------------------------------------------------
//  STAT CARD
// ----------------------------------------------------------------------
@Composable
fun StatCard(
    title: String,
    value: String,
    change: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(155.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, fontSize = 14.sp, color = Color.Gray)
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(change, fontSize = 14.sp, color = Color(0xFF00A63E))
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1A679180)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------------------------
//  BAR CHART
// ----------------------------------------------------------------------
@Composable
fun OccupancyChart() {
    Column(Modifier.fillMaxWidth()) {

        Row(
            Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {

            val bars = listOf(0.55f, 0.48f, 0.31f, 0.14f, 0.02f, 0.11f, 0.28f, 0.55f)

            bars.forEach { h ->
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height((180 * h).dp)
                        .background(Color(0xFF679180), RoundedCornerShape(6.dp))
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
            listOf("6AM", "8AM", "12PM", "2PM", "4PM", "6PM", "8PM").forEach {
                Text(it, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// ----------------------------------------------------------------------
//  IDENTICAL BOTTOM BAR (EXACT COPY OF ADMIN SETTINGS)
// ----------------------------------------------------------------------
@Composable
fun AdminDashboardBottomBar(
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

    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
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
                    Icon(icon, label, tint = color, modifier = Modifier.size(22.dp))
                    Text(label, color = color, fontSize = 12.sp)
                }
            }
        }
    }
}
