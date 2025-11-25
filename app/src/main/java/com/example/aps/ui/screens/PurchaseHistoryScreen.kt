package com.example.aps.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aps.R

// ------------------- DATA MODEL -------------------
data class TransactionItem(
    val title: String,
    val description: String,
    val amount: String,
    val status: String,
    val isConfirmed: Boolean,
    val date: String,
    val icon: Int = R.drawable.ic_nav_payments
)

// ------------------- SCREEN -------------------
@Composable
fun PurchaseHistoryScreen(navController: NavController) {

    val transactions = listOf(
        TransactionItem(
            "Cash-in",
            "From ABC Bank ATM",
            "$100.00",
            "confirmed",
            true,
            "17 Sep 2023 • 10:34 AM"
        ),
        TransactionItem(
            "Cashback from purchase",
            "Purchase from Amazon.com",
            "$1.75",
            "confirmed",
            true,
            "16 Sep 2023 • 16:08 PM"
        ),
        TransactionItem(
            "Transfer to card",
            "",
            "$9000.00",
            "confirmed",
            true,
            "16 Sep 2023 • 11:21 AM"
        ),
        TransactionItem(
            "Transfer to card",
            "Not enough funds",
            "$9267.00",
            "canceled",
            false,
            "15 Sep 2023 • 10:11 AM"
        ),
        TransactionItem(
            "Cashback from purchase",
            "Purchase from Books.com",
            "$3.21",
            "confirmed",
            true,
            "14 Sep 2023 • 18:59 PM"
        ),
        TransactionItem(
            "Transfer to card",
            "",
            "$70.00",
            "confirmed",
            true,
            "13 Sep 2023 • 10:21 AM"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7F8))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {

            // ---------- TOP PADDING ----------
            Spacer(modifier = Modifier.height(24.dp))

            // ---------- TOP CARD SELECTOR ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // VISA CARD BOX
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .background(Color(0xFFFFE1B3), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "VISA",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "ending with ***9749",
                            fontSize = 13.sp,
                            color = Color(0xFFFFFFFF)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_down),
                            contentDescription = null,
                            tint = Color(0xFFFFFFFF),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // RIGHT: SEARCH + FILTER
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = null,
                            tint = Color(0xFF363636),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter),
                            contentDescription = null,
                            tint = Color(0xFF363636),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- LIST ----------
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(transactions) { item ->
                    TransactionCard(item)
                }
            }
        }

        // ---------- BOTTOM NAV ----------
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(70.dp)
                .background(Color.White)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Home",
                imageRes = R.drawable.ic_nav_home,
                onClick = { navController.navigate("user_dashboard") }
            )
            BottomNavItem(
                label = "Payments",
                imageRes = R.drawable.ic_nav_payments,
                selected = true,
                onClick = { navController.navigate("purchase_history") }
            )
            BottomNavItem(
                label = "Loyalty",
                imageRes = R.drawable.ic_nav_loyalty,
                onClick = { navController.navigate("loyalty") }
            )
            BottomNavItem(
                label = "Profile",
                imageRes = R.drawable.ic_nav_profile,
                onClick = { navController.navigate("profile") }
            )
        }
    }
}


// ------------------- ITEM CARD -------------------
@Composable
fun TransactionCard(item: TransactionItem) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ICON BOX
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF85BCA5).copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = item.icon),
                    contentDescription = null,
                    tint = Color(0xFF85BCA5),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

                if (item.description.isNotEmpty()) {
                    Text(item.description, fontSize = 13.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.status,
                        fontSize = 12.sp,
                        color = if (item.isConfirmed) Color(0xFF1CA85C) else Color(0xFFE74949)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        item.date,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Text(
                item.amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}
