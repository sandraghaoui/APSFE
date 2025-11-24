package com.example.aps.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.unit.sp

/**
 * SETTINGS CARD
 */
@Composable
fun SettingsCard(
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
 * EDIT BUTTON
 */
@Composable
fun EditButton(onClick: () -> Unit = {}) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF3CA3FF))
    ) {
        Text("EDIT", fontWeight = FontWeight.Medium)
        Icon(
            Icons.Default.Edit,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * BOTTOM NAVIGATION
 */
@Composable
fun AdminSettingsBottomBar(
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
                                "adjustments" -> navController.navigate("admin_settings")
                                "finances" -> navController.navigate("financial_reports")
                                "reservations" -> navController.navigate("admin_reservations")
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

/**
 * MAIN SCREEN
 */
@Composable
fun AdminSettingsScreen(navController: NavController) {

    var activeTab by remember { mutableStateOf("adjustments") }

    Scaffold(
        bottomBar = {
            AdminSettingsBottomBar(
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
                .background(MaterialTheme.colorScheme.background)
        ) {

            // HEADER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Text(
                    "Admin Settings",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Samer's Parking",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Wednesday, October 1, 2025",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // CONTENT
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Parking Spots
                Text("Parking Spots", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Current Occupancy", color = Color.Gray)
                            Spacer(Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("247/300", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(12.dp))

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.DirectionsCar, null, tint = Color.Gray)
                                    }
                                }
                            }

                            Text("+12 from yesterday", color = Color(0xFF22C55E))
                        }
                        EditButton()
                    }
                }

                // Pricing Adjustments
                Text("Pricing Adjustments", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Price/Hour", color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("$5", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(12.dp))

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.AttachMoney, null, tint = Color.Gray)
                                    }
                                }
                            }
                        }
                        EditButton()
                    }
                }

                // ⭐⭐⭐ LOYALTY SETTINGS (RESTORED)
                Text("Loyalty Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                SettingsCard {
                    Text(
                        "You can activate loyalty rewards for your customers to benefit from choosing you.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Learn more",
                        color = Color(0xFF3CA3FF),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3CD6B7),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Activate", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
