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

        // Edge-to-edge: allow content behind system bars
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
                // leave space so content isn't hidden behind bottom bar
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

                // Green overlay with transparency so image shows through
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
                    // Logo in a circle
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

            Spacer(Modifier.height(40.dp))

            // ---------- BOTTOM BIG GRADIENT CARD ----------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GreenLight, GreenBottom),
                            start = Offset(0f, 0f),           // top-left
                            end = Offset(1000f, 1000f)        // bottom-right
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
                        onClick = { /* TODO: maybe go to sign up later */ },
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
                onClick = {

                },
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
                        // For now, just go back to home or later to a real sign-up screen
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
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
    selected: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
