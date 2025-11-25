package com.example.aps.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aps.R

/**
 * HomeScreen - Login/Welcome screen for all users (no navbar)
 * This is the entry point before authentication
 */
@Composable
fun HomeScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    
    val greenLight = Color(0xFF85BCA5)
    val greenTop = Color(0xFF354E44)
    val greenBottom = Color(0xFF263931)
    
    // Responsive dimensions
    val headerHeight = (screenHeight * 0.35f).coerceAtMost(300.dp).coerceAtLeast(200.dp)
    val horizontalPadding = (screenWidth * 0.06f).coerceAtMost(24.dp).coerceAtLeast(16.dp)
    val verticalPadding = (screenHeight * 0.03f).coerceAtMost(32.dp).coerceAtLeast(16.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    verticalArrangement = Arrangement.Center,
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

                    Spacer(Modifier.height(16.dp))

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
                modifier = Modifier.fillMaxWidth(),
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

            // ---------------- LOGIN/SIGNUP CARD ----------------
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
                        text = "Get Started Today",
                        color = Color.White,
                        fontSize = (screenWidth.value * 0.045f).coerceIn(16f, 18f).sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Join thousands of happy parkers and\nstart earning rewards",
                        color = Color(0xFFE5F4EF),
                        fontSize = (screenWidth.value * 0.032f).coerceIn(11f, 13f).sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { navController.navigate("signup") },
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
                            "Create Account",
                            fontSize = (screenWidth.value * 0.04f).coerceIn(14f, 16f).sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((screenHeight * 0.06f).coerceAtMost(56.dp).coerceAtLeast(48.dp)),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = greenLight,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "Log In",
                            fontSize = (screenWidth.value * 0.04f).coerceIn(14f, 16f).sp
                        )
                    }
                }
                Spacer(Modifier.height(verticalPadding * 2f))
            }
        }
    }
}

// BottomNavItem moved to UserDashboard.kt
