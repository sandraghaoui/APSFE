package com.example.aps.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aps.R
import com.example.aps.api.ApiService
import com.example.aps.api.RetrofitClient
import com.example.aps.api.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PURCHASE_HISTORY_TAG = "PurchaseHistoryScreen"

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

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val api = remember {
        RetrofitClient.getClient { sessionManager.getAccessToken() }
            .create(ApiService::class.java)
    }

    var transactions by remember { mutableStateOf<List<TransactionItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        Log.d(PURCHASE_HISTORY_TAG, "Starting payments fetch")
        isLoading = true
        errorMessage = null
        try {
            val peopleResp = api.getMyPeople()
            Log.d(
                PURCHASE_HISTORY_TAG,
                "getMyPeople success=${peopleResp.isSuccessful} code=${peopleResp.code()}"
            )
            if (!peopleResp.isSuccessful || peopleResp.body() == null) {
                val errorBody = peopleResp.errorBody()?.string() ?: "Unknown error"
                errorMessage = "Unable to load your profile: $errorBody"
                Log.e(PURCHASE_HISTORY_TAG, "Failed to load profile: $errorBody")
                isLoading = false
                return@LaunchedEffect
            }
            val userUuid = peopleResp.body()!!.uuid
            Log.d(PURCHASE_HISTORY_TAG, "Resolved user UUID $userUuid")

            val reservationsResp = api.listReservations(peopleUuid = userUuid)
            Log.d(
                PURCHASE_HISTORY_TAG,
                "listReservations success=${reservationsResp.isSuccessful} code=${reservationsResp.code()} size=${reservationsResp.body()?.size}"
            )
            if (!reservationsResp.isSuccessful || reservationsResp.body() == null) {
                val errorBody = reservationsResp.errorBody()?.string() ?: "Unknown error"
                errorMessage = "Unable to load reservations: $errorBody"
                Log.e(PURCHASE_HISTORY_TAG, "Failed reservations fetch: $errorBody")
                isLoading = false
                return@LaunchedEffect
            }
            val userReservations = reservationsResp.body()!!
                .filter { it.people_uuid == userUuid }
            Log.d(
                PURCHASE_HISTORY_TAG,
                "User reservations count=${userReservations.size}"
            )

            val parkingResp = api.listParkings()
            Log.d(
                PURCHASE_HISTORY_TAG,
                "listParkings success=${parkingResp.isSuccessful} code=${parkingResp.code()} size=${parkingResp.body()?.size}"
            )
            val parkingMap = if (parkingResp.isSuccessful && parkingResp.body() != null) {
                parkingResp.body()!!.associateBy { it.name }
            } else emptyMap()
            Log.d(PURCHASE_HISTORY_TAG, "Mapped ${parkingMap.size} parkings")

            val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val spaceParser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())

            transactions = userReservations
                .sortedByDescending { it.time }
                .map { res ->
                    val parsedDate = parseReservationTime(res.time, isoParser, spaceParser)
                    val dateText = parsedDate?.let { formatter.format(it) } ?: res.time
                    val parking = parkingMap[res.parking_id]
                    val status = res.status.ifBlank { "pending" }
                    val normalized = status.lowercase()

                    TransactionItem(
                        title = parking?.name ?: res.parking_id,
                        description = parking?.location ?: "Parking ${res.parking_id}",
                        amount = String.format(Locale.getDefault(), "$%.2f", res.price),
                        status = status,
                        isConfirmed = normalized !in listOf("canceled", "cancelled", "failed"),
                        date = dateText
                    )
                }
            Log.d(
                PURCHASE_HISTORY_TAG,
                "Prepared ${transactions.size} transaction items"
            )
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unexpected error"
            Log.e(PURCHASE_HISTORY_TAG, "Error loading payments", e)
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7F8))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {

            // ---------- LIST ----------
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFE53935),
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
                transactions.isEmpty() -> {
                    Text(
                        text = "No payments yet. Start by making a reservation.",
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(transactions) { item ->
                            TransactionCard(item)
                        }
                    }
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


private fun parseReservationTime(
    raw: String,
    isoParser: SimpleDateFormat,
    spaceParser: SimpleDateFormat
): Date? {
    return try {
        if (raw.contains("T")) {
            isoParser.parse(raw)
        } else {
            spaceParser.parse(raw)
        }
    } catch (e: Exception) {
        null
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
