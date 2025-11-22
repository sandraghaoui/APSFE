package com.example.aps.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun AdminDashboard(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // slate-50
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp, 24.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Samer's Parking",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF1E293B) // slate-800
                )
                Text(
                    text = "Sunday, September 28, 2025",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF717182)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Cards Grid (2x2)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // First Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Current Occupancy Card
                    StatCard(
                        title = "Current Occupancy",
                        value = "247/300",
                        change = "+12 from yesterday",
                        iconRes = R.drawable.ic_car,
                        modifier = Modifier.weight(1f)
                    )

                    // Active Reservations Card
                    StatCard(
                        title = "Active Reservations",
                        value = "45",
                        change = "+5 today",
                        iconRes = R.drawable.ic_calendar,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Second Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Daily Revenue Card
                    StatCard(
                        title = "Daily Revenue",
                        value = "$2,450",
                        change = "+8.5%",
                        iconRes = R.drawable.ic_revenue,
                        modifier = Modifier.weight(1f)
                    )

                    // Total Customers Card
                    StatCard(
                        title = "Total Customers",
                        value = "892",
                        change = "+23 this week",
                        iconRes = R.drawable.ic_users,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Occupancy Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(17.33.dp)
                ) {
                    Text(
                        text = "Today's Occupancy",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1E293B) // slate-800
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Simple bar chart representation
                    OccupancyChart()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { navController.navigate("home") }) {
                Text("Back to Home")
            }
        }

        // Bottom Navigation Bar
        BottomNavigator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(17.33.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF717182),
                        lineHeight = 24.sp
                    )
                    Text(
                        text = value,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1E293B), // slate-800
                        lineHeight = 32.sp
                    )
                    Text(
                        text = change,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF00A63E), // green
                        lineHeight = 20.sp
                    )
                }

                // Icon Container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x1A679180)), // rgba(103,145,128,0.1)
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OccupancyChart() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Chart area with bars
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(192.dp)
        ) {
            // Y-axis labels (right side)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text("280", fontSize = 12.sp, color = Color(0xFF64748B)) // slate-500
                Text("210", fontSize = 12.sp, color = Color(0xFF64748B))
                Text("140", fontSize = 12.sp, color = Color(0xFF64748B))
                Text("70", fontSize = 12.sp, color = Color(0xFF64748B))
                Text("0", fontSize = 12.sp, color = Color(0xFF64748B))
            }

            // Chart bars area
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .padding(end = 40.dp)
                    .height(192.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Sample bar chart data (heights as percentages of max)
                val barHeights = listOf(0.55f, 0.48f, 0.31f, 0.14f, 0.02f, 0.11f, 0.28f, 0.55f)

                barHeights.forEach { height ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height((192.dp * height))
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF679180)) // green color
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-axis labels (time)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("6AM", "8AM", "12PM", "2PM", "4PM", "6PM", "8PM").forEach { time ->
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B) // slate-500
                )
            }
        }
    }
}

@Composable
fun BottomNavigator(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(73.dp)
            .border(
                width = 1.33.dp,
                color = Color(0x1A000000), // rgba(0,0,0,0.1)
                shape = RoundedCornerShape(0.dp)
            ),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 9.5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dashboard (selected)
            AdminBottomNavItem(
                label = "Dashboard",
                iconRes = R.drawable.ic_dashboard,
                selected = true
            )

            // Reservations
            AdminBottomNavItem(
                label = "Reservations",
                iconRes = R.drawable.ic_clock,
                selected = false
            )

            // Finances
            AdminBottomNavItem(
                label = "Finances",
                iconRes = R.drawable.ic_money,
                selected = false
            )

            // Adjustments
            AdminBottomNavItem(
                label = "Adjustments",
                iconRes = R.drawable.ic_settings,
                selected = false
            )
        }
    }
}

@Composable
fun AdminBottomNavItem(
    label: String,
    iconRes: Int,
    selected: Boolean
) {
    Column(
        modifier = Modifier
            .height(56.dp)
            .clickable { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .then(
                    if (selected) {
                        Modifier.background(
                            color = Color(0x1A679180), // rgba(103,145,128,0.1)
                            shape = RoundedCornerShape(10.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = if (selected) Color(0xFF679180) else Color(0xFF717182)
        )
    }
}
