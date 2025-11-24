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

/**
 * -------------------------------
 * BOTTOM NAVIGATION (working navigation)
 * -------------------------------
 */
@Composable
fun FinancialBottomBar(
    activeTab: String,
    onTabChange: (String) -> Unit,
    navController: NavController?
) {
    val navItems = listOf(
        Triple("dashboard", "Dashboard", Icons.Default.BarChart),
        Triple("reservations", "Reservations", Icons.Default.AccessTime),
        Triple("finances", "Finances", Icons.Default.CreditCard),
        Triple("adjustments", "Adjustments", Icons.Default.Settings)
    )

    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { (value, label, icon) ->

                val selected = value == activeTab
                val color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Gray

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            onTabChange(value)

                            when (value) {
                                "dashboard" -> navController?.navigate("admin")
                                "adjustments" -> navController?.navigate("admin_settings")
                                "finances" -> navController?.navigate("finances")
                                "reservations" -> navController?.navigate("admin_reservations")
                                else -> "dashboard"
                            }
                        }
                        .padding(4.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(text = label, color = color, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * -------------------------------
 * FINANCIAL REPORTS MAIN SCREEN
 * -------------------------------
 */
@Composable
fun FinancialReportsScreen(navController: NavController? = null) {

    var activeTab by remember { mutableStateOf("finances") }

    Scaffold(
        bottomBar = {
            FinancialBottomBar(
                activeTab = activeTab,
                onTabChange = { activeTab = it },
                navController = navController
            )
        }
    ) { padding ->

        val chartData = listOf(
            "Sep 11" to 2100,
            "Sep 16" to 1900,
            "Sep 21" to 2300,
            "Sep 26" to 2800,
            "Oct 1" to 2450
        )

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
                    Text("Samer's Parking", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Wednesday, October 1, 2025", color = Color.Gray, fontSize = 13.sp)
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
                            Text("$2,450", fontSize = 32.sp, fontWeight = FontWeight.Bold)
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
                        Text("+8.5%", color = Color(0xFF22C55E), fontWeight = FontWeight.Medium)
                    }
                }

                // Weekly Revenue
                FRSettingsCard {
                    Column {
                        Text("Weekly Revenue", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$13,970", fontSize = 32.sp, fontWeight = FontWeight.Bold)
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
                        Text("-2.5%", color = Color(0xFFEF4444), fontWeight = FontWeight.Medium)
                    }
                }

                // Monthly Revenue
                FRSettingsCard {
                    Column {
                        Text("Monthly Revenue", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$63,310", fontSize = 32.sp, fontWeight = FontWeight.Bold)
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
                        Text("+4.0%", color = Color(0xFF22C55E), fontWeight = FontWeight.Medium)
                    }
                }

                // Chart
                FRSettingsCard {
                    SimpleLineChart(chartData)
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
