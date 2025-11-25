package com.example.aps.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aps.R
import com.example.aps.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(navController: NavController) {
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("Loading...") }
    var phone by remember { mutableStateOf("") }
    var carPlate by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var loyaltyPoints by remember { mutableStateOf(0) }
    var balance by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch user profile on screen load
    LaunchedEffect(Unit) {
        authViewModel.getUserProfile(
            context = context,
            onSuccess = { user, people ->
                fullName = "${user.first_name} ${user.last_name}"
                phone = user.phone_number
                email = user.email
                carPlate = people.plate_number.toString()
                loyaltyPoints = people.loyalty_points
                balance = people.balance
                isLoading = false
            },
            onError = { error ->
                Toast.makeText(context, "Failed to load profile: $error", Toast.LENGTH_LONG).show()
                isLoading = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ---- PROFILE IMAGE ----
            Box(
                modifier = Modifier.size(115.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile_placeholder),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(115.dp)
                        .clip(CircleShape)
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEE8924))
                        .padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(email, fontSize = 13.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                // Loyalty & Balance Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoCard("Loyalty Points", loyaltyPoints.toString())
                    InfoCard("Balance", "$${"%.2f".format(balance)}")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ---- FULL NAME ----
            ProfileField(
                label = "Full Name",
                value = fullName,
                placeholder = "Full Name",
                onChange = { fullName = it },
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- PHONE ----
            ProfileField(
                label = "Phone Number",
                value = phone,
                icon = R.drawable.ic_call,
                placeholder = "Phone",
                keyboard = KeyboardType.Phone,
                onChange = { phone = it },
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- CAR PLATE ----
            ProfileField(
                label = "Car Plate",
                value = carPlate,
                icon = R.drawable.ic_circle_lock,
                placeholder = "Car Plate",
                onChange = { carPlate = it },
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- EMAIL ----
            ProfileField(
                label = "Your Email",
                value = email,
                icon = R.drawable.ic_mail,
                placeholder = "Email",
                keyboard = KeyboardType.Email,
                onChange = { email = it },
                enabled = false
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ---- LOGOUT BUTTON ----
            Button(
                onClick = {
                    authViewModel.logout(context)
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF85BCA5)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF85BCA5))
            ) {
                Text("Logout", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(90.dp))
        }

        // ---- BOTTOM NAV ----
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
                selected = true,
                onClick = { navController.navigate("profile") }
            )
        }
    }
}

@Composable
fun InfoCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    icon: Int? = null,
    placeholder: String,
    keyboard: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF262422))

        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = Color(0xFFABABAB)) },
            leadingIcon = if (icon != null) {
                {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE7E7E7),
                focusedBorderColor = Color(0xFFE7E7E7),
                disabledBorderColor = Color(0xFFE7E7E7),
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF5F5F5)
            )
        )
    }
}