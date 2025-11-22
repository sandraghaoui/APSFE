package com.example.aps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") { SplashScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("admin") { AdminDashboardScreen(navController) }
        composable("adminReservations") { AdminReservationsScreen(navController) }
    }
}

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = "Automated\nParking System",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Image(
                painter = painterResource(id = R.drawable.apslogo),
                contentDescription = "Automated Parking System Logo",
                modifier = Modifier
                    .width(180.dp)
                    .height(400.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Here we Park!",
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    val GreenLight = Color(0xFF85BCA5)
    val GreenTop = Color(0xFF354E44)
    val GreenBottom = Color(0xFF263931)
    val bottomBarHeight = 60.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7F8))
    ) {
        // ---------- SCROLLABLE CONTENT ----------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomBarHeight)
        ) {
            // ---------- TOP HEADER (image + gradient) ----------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
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
                                    GreenLight.copy(alpha = 0.65f),
                                    GreenTop.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFFF6F7F8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.apslogo),
                            contentDescription = "APS logo",
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "Welcome to APS",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Automated Parking System",
                        color = Color(0xFFE5F4EF),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---------- MIDDLE TEXT ----------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Find Your Perfect Parking Spot",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Real-time availability, instant booking, and rewards",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(34.dp))

            // ---------- 3 FEATURE CARDS ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    title = "Real-Time\nUpdates",
                    imageRes = R.drawable.ic_feature_realtime,
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    title = "Secure\nParking",
                    imageRes = R.drawable.ic_feature_secure,
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    title = "Earn\nRewards",
                    imageRes = R.drawable.ic_feature_rewards,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ---------- SMALL BUTTON TO ACCESS ADMIN ----------
            OutlinedButton(
                onClick = { navController.navigate("admin") },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF85BCA5)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF263931)
                )
            ) {
                Text(
                    text = "Admin dashboard (temp)",
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---------- BOTTOM BIG GRADIENT CARD ----------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GreenLight, GreenBottom),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Get Started Today",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Join thousands of happy parkers and\nstart earning rewards",
                        color = Color(0xFFE5F4EF),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { /* TODO: sign up */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = GreenBottom
                        )
                    ) {
                        Text("Create Account")
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Log In")
                    }
                }
            }
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
                selected = true
            )
            BottomNavItem(
                label = "Payments",
                imageRes = R.drawable.ic_nav_payments
            )
            BottomNavItem(
                label = "Loyalty",
                imageRes = R.drawable.ic_nav_loyalty
            )
            BottomNavItem(
                label = "Profile",
                imageRes = R.drawable.ic_nav_profile
            )
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {
    val GreenLight = Color(0xFF85BCA5)
    val background = Color(0xFFF6F7F8)

    val email = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Log In To your Account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(42.dp))

            // Email
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Your Email", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "SariLakkis@gmail.com",
                            color = Color(0xFFB8C2CC)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Phone
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Phone Number", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(
                    value = phone.value,
                    onValueChange = { phone.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "+93123135",
                            color = Color(0xFFB8C2CC)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Password
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Password", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "************",
                            color = Color(0xFFB8C2CC)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(Modifier.height(35.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenLight,
                    contentColor = Color.White
                )
            ) {
                Text("Log in")
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Sign up!",
                    fontSize = 13.sp,
                    color = Color(0xFF3366FF),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

// --------------------- ADMIN DASHBOARD -----------------------

@Composable
fun AdminDashboardScreen(navController: NavHostController) {
    val background = Color(0xFFF6F7F8)
    val GreenAccent = Color(0xFF85BCA5)
    val bottomBarHeight = 60.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp, bottom = bottomBarHeight + 8.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Samer's Parking",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sunday, September 28, 2025",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "Back",
                    fontSize = 12.sp,
                    color = GreenAccent,
                    modifier = Modifier.clickable {
                        navController.popBackStack()
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            // First row cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard(
                    title = "Current\nOccupancy",
                    mainValue = "247/300",
                    subValue = "+12 from yesterday",
                    subColor = Color(0xFF4CAF50),
                    emoji = "🚗",
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Active\nReservations",
                    mainValue = "45",
                    subValue = "+5 today",
                    subColor = Color(0xFF4CAF50),
                    emoji = "⏱️",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Second row cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard(
                    title = "Daily\nRevenue",
                    mainValue = "$2,450",
                    subValue = "+8.5%",
                    subColor = Color(0xFF4CAF50),
                    emoji = "💵",
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Total\nCustomers",
                    mainValue = "892",
                    subValue = "+23 this week",
                    subColor = Color(0xFF4CAF50),
                    emoji = "👥",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(28.dp))

            OccupancyChartCard()
        }

        // Bottom nav (admin area)
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
                label = "Dashboard",
                imageRes = R.drawable.ic_nav_loyalty,
                selected = true,
                onClick = { /* already here */ }
            )
            BottomNavItem(
                label = "Reservations",
                imageRes = R.drawable.ic_nav_time,
                onClick = { navController.navigate("adminReservations") }
            )
            BottomNavItem(
                label = "Finances",
                imageRes = R.drawable.ic_nav_payments
            )
            BottomNavItem(
                label = "Adjustments",
                imageRes = R.drawable.ic_nav_adjustments
            )
        }
    }
}

// --------------------- ADMIN RESERVATIONS --------------------

data class Reservation(
    val plate: String,
    val status: String,
    val statusColor: Color,
    val price: String,
    val customerName: String,
    val spot: String,
    val timeRange: String
)

@Composable
fun AdminReservationsScreen(navController: NavHostController) {
    val background = Color(0xFFF6F7F8)
    val bottomBarHeight = 60.dp

    val reservations = listOf(
        Reservation(
            plate = "A46123",
            status = "active",
            statusColor = Color(0xFF16C172),
            price = "$12",
            customerName = "Sandra Ghaoui",
            spot = "A-15",
            timeRange = "09:00 - 17:00"
        ),
        Reservation(
            plate = "X22789",
            status = "active",
            statusColor = Color(0xFF16C172),
            price = "$5",
            customerName = "Taylor Swift",
            spot = "B-22",
            timeRange = "10:30 - 14:30"
        ),
        Reservation(
            plate = "D15456",
            status = "upcoming",
            statusColor = Color(0xFF246BFD),
            price = "$10",
            customerName = "Lebron James",
            spot = "C-08",
            timeRange = "14:00 - 18:00"
        ),
        Reservation(
            plate = "G00789",
            status = "active",
            statusColor = Color(0xFF16C172),
            price = "$8",
            customerName = "Lionel Messi",
            spot = "A-03",
            timeRange = "11:00 - 15:00"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp, bottom = bottomBarHeight + 8.dp)
        ) {
            Text(
                text = "Active Reservations",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            reservations.forEach { res ->
                ReservationCard(reservation = res)
                Spacer(Modifier.height(12.dp))
            }
        }

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
                label = "Dashboard",
                imageRes = R.drawable.ic_nav_loyalty,
                onClick = { navController.navigate("admin") }
            )
            BottomNavItem(
                label = "Reservations",
                imageRes = R.drawable.ic_nav_time,
                selected = true,
                onClick = { /* already here */ }
            )
            BottomNavItem(
                label = "Finances",
                imageRes = R.drawable.ic_nav_payments
            )
            BottomNavItem(
                label = "Adjustments",
                imageRes = R.drawable.ic_nav_adjustments
            )
        }
    }
}

@Composable
fun ReservationCard(reservation: Reservation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP PART (plate + status + price + name)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(Color(0xFFF2F5F7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🚗", fontSize = 16.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = reservation.plate,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(reservation.statusColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = reservation.status,
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 2.dp
                                )
                            )
                        }
                    }

                    Text(
                        text = reservation.price,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = reservation.customerName,
                    fontSize = 17.sp
                )
            }

            // BOTTOM ROW — ALWAYS AT BOTTOM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spot: ${reservation.spot}",
                    fontSize = 15.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏱️", fontSize = 15.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = reservation.timeRange,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}


// --------------------- OTHER REUSABLE COMPONENTS -------------------

@Composable
fun AdminStatCard(
    title: String,
    mainValue: String,
    subValue: String,
    subColor: Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = mainValue,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFFF2F5F7), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 24.sp)
                }
            }

            Text(
                text = subValue,
                fontSize = 15.sp,
                color = subColor
            )
        }
    }
}

@Composable
fun OccupancyChartCard() {
    val cardHeight = 280.dp
    val barMaxHeight = 140.dp

    val hours = listOf("6AM", "8AM", "10AM", "12PM", "2PM", "4PM", "6PM", "8PM")
    val values = listOf(20, 70, 140, 210, 260, 230, 150, 80)
    val maxValue = values.maxOrNull() ?: 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Today's Occupancy",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barMaxHeight),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                values.forEach { v ->
                    val ratio = v.toFloat() / maxValue.toFloat()
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(barMaxHeight * ratio)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF85BCA5).copy(alpha = 0.7f))
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                hours.forEach { h ->
                    Text(
                        text = h,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    imageRes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(140.dp),
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
                modifier = Modifier
                    .size(56.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 13.sp,
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
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = label,
            modifier = Modifier
                .size(22.dp)
                .alpha(if (selected) 1f else 0.5f)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) Color(0xFF85BCA5) else Color.Gray
        )
    }
}
