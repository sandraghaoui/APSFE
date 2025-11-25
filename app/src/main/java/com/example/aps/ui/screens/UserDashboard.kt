package com.example.aps.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aps.CameraActivity
import com.example.aps.R

/**
 * UserDashboard - Main screen for regular users after login
 * Contains feature cards, parking options, and bottom navigation
 */
@Composable
fun UserDashboard(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    
    val greenLight = Color(0xFF85BCA5)
    val greenTop = Color(0xFF354E44)
    val greenBottom = Color(0xFF263931)
    val bottomBarHeight = 70.dp

    val context = LocalContext.current
    
    // Responsive dimensions
    val horizontalPadding = (screenWidth * 0.04f).coerceAtMost(16.dp).coerceAtLeast(12.dp)
    val verticalPadding = (screenHeight * 0.02f).coerceAtMost(24.dp).coerceAtLeast(16.dp)
    val headerHeight = (screenHeight * 0.3f).coerceAtMost(260.dp).coerceAtLeast(180.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomBarHeight)
        ) {
            // ---------------- HEADER ----------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.home_header),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    greenLight.copy(alpha = 0.65f),
                                    greenTop.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size((headerHeight * 0.25f).coerceAtMost(60.dp))
                            .background(Color(0xFFF6F7F8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.apslogo),
                            contentDescription = "APS logo",
                            modifier = Modifier.size((headerHeight * 0.2f).coerceAtMost(40.dp))
                        )
                    }

                    Text(
                        text = "Welcome to APS",
                        color = Color.White,
                        fontSize = (screenWidth.value * 0.065f).coerceIn(20f, 26f).sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Automated Parking System",
                        color = Color(0xFFE5F4EF),
                        fontSize = (screenWidth.value * 0.035f).coerceIn(12f, 14f).sp
                    )
                }
            }

            Spacer(Modifier.height(verticalPadding))

            // ---------------- SUBTITLE ----------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding * 1.5f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Find Your Perfect Parking Spot",
                    fontSize = (screenWidth.value * 0.045f).coerceIn(16f, 18f).sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Real-time availability, instant booking, and rewards",
                    fontSize = (screenWidth.value * 0.032f).coerceIn(11f, 13f).sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(verticalPadding * 1.5f))

            // ---------------- FEATURE CARDS ----------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    title = "Real-Time\nUpdates",
                    imageRes = R.drawable.ic_feature_realtime,
                    modifier = Modifier.weight(1f),
                    screenWidth = screenWidth
                )
                FeatureCard(
                    title = "Secure\nParking",
                    imageRes = R.drawable.ic_feature_secure,
                    modifier = Modifier.weight(1f),
                    screenWidth = screenWidth
                )
                FeatureCard(
                    title = "Earn\nRewards",
                    imageRes = R.drawable.ic_feature_rewards,
                    modifier = Modifier.weight(1f),
                    screenWidth = screenWidth
                )
            }

            Spacer(Modifier.height(verticalPadding * 2.5f))

            // ---------------- ACTION CARD ----------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(greenLight, greenBottom),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    )
                    .padding(horizontal = horizontalPadding * 1.5f, vertical = verticalPadding)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Quick Actions",
                        color = Color.White,
                        fontSize = (screenWidth.value * 0.045f).coerceIn(16f, 18f).sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Find parking or scan your plate",
                        color = Color(0xFFE5F4EF),
                        fontSize = (screenWidth.value * 0.032f).coerceIn(11f, 13f).sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val intent = Intent(context, CameraActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((screenHeight * 0.06f).coerceAtMost(56.dp).coerceAtLeast(48.dp)),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = greenBottom
                        )
                    ) {
                        Text(
                            "Scan License Plate",
                            fontSize = (screenWidth.value * 0.04f).coerceIn(14f, 16f).sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(verticalPadding * 2f))
        }

        // ---------------- BOTTOM NAV ----------------
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomBarHeight)
                .background(Color.White)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Home",
                imageRes = R.drawable.ic_nav_home,
                selected = true,
                onClick = { navController.navigate("user_dashboard") }
            )
            BottomNavItem(
                label = "Payments",
                imageRes = R.drawable.ic_nav_payments,
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

@Composable
fun FeatureCard(
    title: String,
    imageRes: Int,
    modifier: Modifier = Modifier,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    Card(
        modifier = modifier.height((screenWidth.value * 0.35f).dp.coerceIn(100.dp, 140.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.size((screenWidth.value * 0.14f).dp.coerceIn(40.dp, 56.dp))
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = (screenWidth.value * 0.032f).coerceIn(11f, 13f).sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BottomNavItem(
    label: String,
    imageRes: Int,
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
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = label,
                modifier = Modifier
                    .size(22.dp)
                    .alpha(if (selected) 1f else 0.5f)
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


