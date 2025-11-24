package com.example.aps.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aps.R
import androidx.compose.ui.graphics.Color

@Composable
fun ConfirmBookingScreen(
    navController: NavController,
    parkName: String = "Yasser's Parking",
    parkAddress: String = "Bliss, behind Food District",
    rating: String = "4.5",
    etaText: String = "15min",
    distanceText: String = "2.3km",
    reservationPeriod: String = "Oct 1, 10:54 AM - 12:34 AM",
    rateText: String = "$5 full day",
    totalAmountText: String = "$10",
    onConfirm: () -> Unit = {}
) {
    // Colors & spacing follow your app patterns
    val pagePadding = 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = pagePadding, vertical = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top bar (back + title)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.background(
                    color = Color(0xFFE2E8F0),
                    shape = CircleShape
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = Color(0xFF32403A),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = "Confirm Booking",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Review your parking details",
                    fontSize = 13.sp,
                    color = Color(0xFF8A8F94)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Map / image with badges
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            // Replace map_placeholder with your map/image drawable
            Image(
                painter = painterResource(id = R.drawable.map_placeholder),
                contentDescription = "Route map",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // ETA badge (top-right)
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_direction), // small car icon
                        contentDescription = null,
                        tint = Color(0xFF8A8F94),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(text = "ETA", fontSize = 12.sp, color = Color(0xFF8A8F94))
                        Text(text = etaText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Distance badge (bottom-left)
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_car), // small car icon
                        contentDescription = null,
                        tint = Color(0xFF8A8F94),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Distance", fontSize = 12.sp, color = Color(0xFF8A8F94))
                        Text(text = distanceText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Parking info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(text = parkName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_location),
                                contentDescription = null,
                                tint = Color(0xFF8A8F94),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = parkAddress, color = Color(0xFF8A8F94))
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = rating, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Booking details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Booking Details", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))

                // Reservation period
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null, tint = Color(0xFF8A8F94), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Reservation Period", color = Color(0xFF6E7276))
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = reservationPeriod, color = Color(0xFF8A8F94))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rate
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(painter = painterResource(id = R.drawable.ic_dollar), contentDescription = null, tint = Color(0xFF8A8F94), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Rate", color = Color(0xFF6E7276))
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = rateText, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Buttons: Confirm (green) and Cancel (light)
        Button(
            onClick = {
                onConfirm()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF85BCA5))
        ) {
            Text(text = "Confirm Booking - $totalAmountText", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0))
        ) {
            Text(text = "Cancel", color = Color(0xFF6E7276), fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Keep same bottom navigation area spacing used across your app
        Spacer(modifier = Modifier.weight(1f))
    }
}
