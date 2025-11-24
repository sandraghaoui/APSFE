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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aps.R
import com.example.aps.viewmodel.LoyaltyViewModel

@Composable
fun LoyaltyScreen(
    navController: NavController,
    viewModel: LoyaltyViewModel = viewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val bottomBarHeight = 60.dp

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F7F8)),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading loyalty data...")
            }
            return
        }

        uiState.error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F7F8)),
                contentAlignment = Alignment.Center
            ) {
                Text("Error loading loyalty: ${uiState.error}")
            }
            return
        }
    }

    val currentPoints = uiState.points
    val goalPoints = 500
    val remaining = (goalPoints - currentPoints).coerceAtLeast(0)
    val progress = (currentPoints.toFloat() / goalPoints).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F7F8))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Spacer(Modifier.height(24.dp))

            // ---------- TOP CARD ----------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF9FD9C1),
                                    Color(0xFF30493F)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Your Points Balance",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = currentPoints.toString(),
                                        color = Color.White,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "points",
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_feature_trophee),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Progress to Full Day Free Parking   $remaining pts left",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF85BCA5))
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---------- HOW TO EARN POINTS ----------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {

                    Text(
                        text = "How to Earn Points",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(Modifier.height(36.dp))

                    EarnRow("Each parking booking", "+150 pts")
                    Spacer(Modifier.height(8.dp))

                    EarnRow("Each payment by card", "+75 pts")
                    Spacer(Modifier.height(8.dp))

                    EarnRow("Monthly subscription", "+200 pts")
                    Spacer(Modifier.height(8.dp))

                    EarnRow("Refer a friend", "+150 pts")
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---------- AVAILABLE REWARDS ----------
            Text(
                text = "Available Rewards",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            RewardCard(
                title = "1 Hour Free Parking",
                description = "Get 1 hour of free parking at any location",
                points = 100,
                icon = R.drawable.ic_feature_realtime_orange,
                onRedeem = { /* TODO */ }
            )

            Spacer(Modifier.height(90.dp))
        }

        // ---------- BOTTOM NAV ----------
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomBarHeight)
                .background(Color.White)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem(
                label = "Home",
                imageRes = R.drawable.ic_nav_home,
                onClick = { navController.navigate("home") }
            )
            BottomNavItem(
                label = "Payments",
                imageRes = R.drawable.ic_nav_payments,
                onClick = { navController.navigate("purchase_history") }
            )
            BottomNavItem(
                label = "Loyalty",
                imageRes = R.drawable.ic_nav_loyalty,
                selected = true,
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
@Composable
fun EarnRow(title: String, pts: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 14.sp)
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .background(Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                pts,
                fontSize = 14.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RewardCard(
    title: String,
    description: String,
    points: Int,
    icon: Int,
    onRedeem: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onRedeem() }
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFEFEFEF),
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x11000000),
                spotColor = Color(0x11000000)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF4E5)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Texts
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF222222)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF777777)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Redeem Now",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFFB347)
                    )
                )
            }

            // Points badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFFFB347))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$points pts",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

