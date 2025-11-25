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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aps.R
import com.example.aps.api.ApiService
import com.example.aps.api.RetrofitClient
import com.example.aps.api.SessionManager
import com.example.aps.viewmodel.AdminDashboardViewModel
import com.example.aps.viewmodel.DashboardUiState
import java.text.SimpleDateFormat
import java.util.*

// ----------------------------------------------------------------------
//  ADMIN DASHBOARD MAIN SCREEN
// ----------------------------------------------------------------------
@Composable
fun AdminDashboard(navController: NavController) {

    var activeTab by remember { mutableStateOf("dashboard") }

    // --- Retrofit + ViewModel --------------------------------
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val api = remember {
        RetrofitClient.getClient { sessionManager.getAccessToken() }
            .create(ApiService::class.java)
    }

    val viewModel: AdminDashboardViewModel = remember {
        AdminDashboardViewModel(api)
    }

    val uiState: DashboardUiState by viewModel.state.collectAsState(
        initial = DashboardUiState()
    )

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
                    text = if (uiState.parkingName.isNotBlank())
                        uiState.parkingName
                    else
                        "Loading...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date()),
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
                        value = "${uiState.currentCapacity}/${uiState.maxCapacity}",
                        change = if (uiState.maxCapacity > 0) {
                            "${((uiState.currentCapacity.toFloat() / uiState.maxCapacity) * 100).toInt()}% full"
                        } else "N/A",
                        iconRes = R.drawable.ic_car,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Active Reservations",
                        value = uiState.activeReservations.toString(),
                        change = if (uiState.activeReservations > 0) "Active now" else "No active",
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
                        value = String.format("$%.2f", uiState.todayRevenue),
                        change = if (uiState.todayRevenue > 0) "Today" else "No revenue",
                        iconRes = R.drawable.ic_revenue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Customers",
                        value = uiState.totalCustomers.toString(),
                        change = if (uiState.totalCustomers > 0) "Registered" else "No customers",
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

                    // NOW DYNAMIC
                    OccupancyChart(occupancyByHour = uiState.occupancyByHour)
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
//  BAR CHART (dynamic from reservations)
// ----------------------------------------------------------------------
@Composable
fun OccupancyChart(occupancyByHour: List<Int>) {
    Column(Modifier.fillMaxWidth()) {

        // time labels we use for 8 bars
        val labels = listOf("6AM", "8AM", "10AM", "12PM", "2PM", "4PM", "6PM", "8PM")

        // if ViewModel didn't send data yet, show flat placeholder
        val values = if (occupancyByHour.isNotEmpty()) {
            occupancyByHour
        } else {
            listOf(0, 0, 0, 0, 0, 0, 0, 0)
        }

        val maxVal = (values.maxOrNull() ?: 0).coerceAtLeast(1)

        Row(
            Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            values.forEach { v ->
                val h = v.toFloat() / maxVal // 0..1
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
            labels.forEach {
                Text(it, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// Old AdminDashboardBottomBar removed - using shared AdminBottomNavBar
