package com.example.aps.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.aps.api.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * -------------------------------
 * SETTINGS CARD
 * -------------------------------
 */
@Composable
fun FRSettingsCard(
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
 * -------------------------------
 * CURVED / SMOOTHED LINE CHART
 * -------------------------------
 */
@Composable
fun SimpleLineChart(data: List<Pair<String, Int>>) {
    if (data.isEmpty()) return

    // Avoid division by zero
    val maxValue = data.maxOf { it.second }.coerceAtLeast(1)
    val minValue = data.minOf { it.second }

    Column(modifier = Modifier.fillMaxWidth()) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 12.dp)
        ) {
            if (data.size < 2) return@Canvas

            val chartWidth = size.width
            val chartHeight = size.height

            val xStep = chartWidth / (data.size - 1)

            // Map data points to canvas coordinates
            val points = data.mapIndexed { index, (_, value) ->
                val x = xStep * index
                val normalized = (value - minValue).toFloat() /
                        (maxValue - minValue).coerceAtLeast(1)
                // Use 80% of height so top has some padding
                val y = chartHeight - (normalized * chartHeight * 0.8f)
                Offset(x, y)
            }

            // Slightly smoothed polyline path
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]

                    // Midpoint trick for a softer angle (not full Bezier, but smoother than a sharp knee)
                    val midX = (prev.x + curr.x) / 2f
                    val midY = (prev.y + curr.y) / 2f

                    lineTo(midX, prev.y)
                    lineTo(curr.x, curr.y)
                }
            }

            // Fill under the curve with vertical gradient
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(points.last().x, chartHeight)
                lineTo(points.first().x, chartHeight)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3CD6B7).copy(alpha = 0.35f),
                        Color.Transparent
                    )
                )
            )

            // Draw main line
            drawPath(
                path = linePath,
                color = Color(0xFF3CD6B7),
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )

            // Highlight last point
            val lastPoint = points.last()

            // Vertical indicator line
            drawLine(
                color = Color(0xFF3CD6B7),
                start = Offset(lastPoint.x, lastPoint.y),
                end = Offset(lastPoint.x, chartHeight),
                strokeWidth = 4f
            )

            // Circle marker
            drawCircle(
                color = Color(0xFF3CD6B7),
                radius = 10f,
                center = lastPoint
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-axis labels (dates)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEach { (label, _) ->
                Text(
                    text = label,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Old FinancialBottomBar removed - using shared AdminBottomNavBar

/**
 * -------------------------------
 * FINANCIAL REPORTS MAIN SCREEN
 * -------------------------------
 */
@Composable
fun FinancialReportsScreen(navController: NavController? = null) {

    var activeTab by remember { mutableStateOf("finances") }
    
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val api = remember {
        RetrofitClient.getClient { sessionManager.getAccessToken() }
            .create(ApiService::class.java)
    }
    
    var parkingName by remember { mutableStateOf<String?>(null) }
    var dailyRevenue by remember { mutableStateOf(0.0) }
    var weeklyRevenue by remember { mutableStateOf(0.0) }
    var monthlyRevenue by remember { mutableStateOf(0.0) }
    var chartData by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
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
                        parkingName = parking?.name
                        
                        if (parking != null) {
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val calendar = Calendar.getInstance()
                            
                            // Daily revenue
                            val dailyResp = api.listRevenues(parkingId = parking.name, start = today, end = today)
                            if (dailyResp.isSuccessful && dailyResp.body() != null) {
                                dailyRevenue = dailyResp.body()!!.sumOf { it.revenue }
                            }
                            
                            // Weekly revenue (last 7 days)
                            calendar.add(Calendar.DAY_OF_MONTH, -7)
                            val weekStart = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                            val weeklyResp = api.listRevenues(parkingId = parking.name, start = weekStart, end = today)
                            if (weeklyResp.isSuccessful && weeklyResp.body() != null) {
                                weeklyRevenue = weeklyResp.body()!!.sumOf { it.revenue }
                            }
                            
                            // Monthly revenue (last 30 days)
                            calendar.time = Date()
                            calendar.add(Calendar.DAY_OF_MONTH, -30)
                            val monthStart = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                            val monthlyResp = api.listRevenues(parkingId = parking.name, start = monthStart, end = today)
                            if (monthlyResp.isSuccessful && monthlyResp.body() != null) {
                                monthlyRevenue = monthlyResp.body()!!.sumOf { it.revenue }
                                
                                // Create chart data from last 5 revenue entries
                                val revenues = monthlyResp.body()!!.sortedBy { it.date }.takeLast(5)
                                chartData = revenues.map { rev ->
                                    val date = SimpleDateFormat("MMM d", Locale.getDefault()).format(
                                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(rev.date) ?: Date()
                                    )
                                    date to rev.revenue.toInt()
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

    Scaffold(
        bottomBar = {
            if (navController != null) {
                AdminBottomNavBar(
                    activeTab = activeTab,
                    navController = navController,
                    onTabChange = { activeTab = it }
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(bottom = 80.dp)
        ) {

            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Text("Financial Reports", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(parkingName ?: "Loading...", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(
                        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date()),
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Daily Revenue
                FRSettingsCard {
                    Column {
                        Text("Daily Revenue", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                String.format("$%,.2f", dailyRevenue),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                color = Color(0xFF3CD6B7).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AttachMoney,
                                        contentDescription = null,
                                        tint = Color(0xFF3CD6B7)
                                    )
                                }
                            }
                        }
                        Text(
                            if (dailyRevenue > 0) "Today" else "No revenue",
                            color = Color(0xFF22C55E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Weekly Revenue
                FRSettingsCard {
                    Column {
                        Text("Weekly Revenue", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                String.format("$%,.2f", weeklyRevenue),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                color = Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = Color.DarkGray
                                    )
                                }
                            }
                        }
                        Text(
                            if (weeklyRevenue > 0) "Last 7 days" else "No revenue",
                            color = if (weeklyRevenue > 0) Color(0xFF22C55E) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Monthly Revenue
                FRSettingsCard {
                    Column {
                        Text("Monthly Revenue", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                String.format("$%,.2f", monthlyRevenue),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                color = Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Timeline,
                                        contentDescription = null,
                                        tint = Color.DarkGray
                                    )
                                }
                            }
                        }
                        Text(
                            if (monthlyRevenue > 0) "Last 30 days" else "No revenue",
                            color = if (monthlyRevenue > 0) Color(0xFF22C55E) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Chart
                FRSettingsCard {
                    if (chartData.isNotEmpty()) {
                        SimpleLineChart(chartData)
                    } else {
                        Text("No chart data available", color = Color.Gray)
                    }
                }
            }
        }
    }
}

/**
 * -------------------------------
 * PREVIEW
 * -------------------------------
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewFinancialReports() {
    FinancialReportsScreen(navController = rememberNavController())
}
