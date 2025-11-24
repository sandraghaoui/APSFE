package com.example.aps.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.aps.R


@Composable
fun BookingSuccessScreen(navController: NavHostController) {

    val GreenLight = Color(0xFF85BCA5)
    val GreenDark = Color(0xFF2F4A3F)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // Top Icon
        Image(
            painter = painterResource(id = R.drawable.apslogo),
            contentDescription = "Automated Parking System Logo",
            modifier = Modifier
                .width(72.dp)
                .height(92.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Thank You for\nChoosing APS!",
            textAlign = TextAlign.Center,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your parking spot has been\nsuccessfully reserved",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Loyalty points card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(GreenLight, GreenDark)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_feature_trophee),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Loyalty Points Earned!",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "+25",
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "points",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Keep parking with us to unlock amazing\nrewards",
                    textAlign = TextAlign.Center,
                    color = Color(0xFFE8F5EF),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Booking details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Booking Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location_green),
                        contentDescription = null,
                        modifier = Modifier
                            .width(18.dp)
                            .height(24.dp),
                        tint = Color(0xFF85BCA5)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("1866 Parking Underground")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar_green),
                        contentDescription = null,
                        modifier = Modifier
                            .width(18.dp)
                            .height(24.dp),
                        tint = Color(0xFF85BCA5)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("October 1, 2025 at 10:19 AM")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Duration:")
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF2CC))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("2 hours", color = Color(0xFFCC9300))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Back to home
        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB84D))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_nav_home),
                contentDescription = null,
                modifier = Modifier
                    .width(30.dp)
                    .height(30.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Home", color = Color.White)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
