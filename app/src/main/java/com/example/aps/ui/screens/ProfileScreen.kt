package com.example.aps.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aps.R

@Composable
fun ProfileScreen(navController: NavController) {

    var fullName by remember { mutableStateOf("Jack Celere") }
    var phone by remember { mutableStateOf("+93123135") }
    var carPlate by remember { mutableStateOf("0123456789") }
    var email by remember { mutableStateOf("xxx@gmail.com") }

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
                modifier = Modifier
                    .size(115.dp),
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

            Text("Tyrone", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("@TyroneBusiness", fontSize = 13.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(28.dp))

            // ---- FULL NAME ----
            ProfileField(
                label = "Full Name",
                value = fullName,
                placeholder = "Jack Celere",
                onChange = { fullName = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- PHONE ----
            ProfileField(
                label = "Phone Number",
                value = phone,
                icon = R.drawable.ic_call,
                placeholder = "+93123135",
                keyboard = KeyboardType.Phone,
                onChange = { phone = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- CAR PLATE ----
            ProfileField(
                label = "Car Plate",
                value = carPlate,
                icon = R.drawable.ic_circle_lock,
                placeholder = "0123456789",
                onChange = { carPlate = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- EMAIL ----
            ProfileField(
                label = "Your Email",
                value = email,
                icon = R.drawable.ic_mail,
                placeholder = "xxx@gmail.com",
                keyboard = KeyboardType.Email,
                onChange = { email = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ---- LOGOUT BUTTON ----
            Button(
                onClick = { /* TODO */ },
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
                .height(60.dp)
                .background(Color.White)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            BottomNavItem("Home", R.drawable.ic_nav_home, false) {
                navController.navigate("home")
            }

            BottomNavItem("Payments", R.drawable.ic_nav_payments) {}

            BottomNavItem("Loyalty", R.drawable.ic_nav_loyalty) {
                navController.navigate("loyalty")
            }

            BottomNavItem("Profile", R.drawable.ic_nav_profile, selected = true) {}
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
    onChange: (String) -> Unit
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
            } else null,     // ← THIS FIXES THE GAP

            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),

            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            shape = RoundedCornerShape(12.dp),

            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE7E7E7),
                focusedBorderColor = Color(0xFFE7E7E7),
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )
    }
}
