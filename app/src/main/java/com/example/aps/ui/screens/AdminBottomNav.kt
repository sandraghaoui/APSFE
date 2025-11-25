package com.example.aps.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * Shared Admin Bottom Navigation Bar
 * Matches user flow style with background highlight for selected items
 */
@Composable
fun AdminBottomNavBar(
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
        color = Color.White,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { (key, label, icon) ->
                AdminBottomNavItem(
                    label = label,
                    icon = icon,
                    selected = key == activeTab,
                    onClick = {
                        onTabChange(key)
                        when (key) {
                            "dashboard" -> navController.navigate("admin") {
                                popUpTo("admin") { saveState = true }
                            }
                            "reservations" -> navController.navigate("admin_reservations") {
                                popUpTo("admin") { saveState = true }
                            }
                            "finances" -> navController.navigate("financial_reports") {
                                popUpTo("admin") { saveState = true }
                            }
                            "adjustments" -> navController.navigate("admin_settings") {
                                popUpTo("admin") { saveState = true }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AdminBottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val greenColor = Color(0xFF85BCA5)
    val backgroundColor = if (selected) greenColor.copy(alpha = 0.15f) else Color.Transparent
    
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp),
                tint = if (selected) greenColor else Color.Gray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) greenColor else Color.Gray
            )
        }
    }
}


