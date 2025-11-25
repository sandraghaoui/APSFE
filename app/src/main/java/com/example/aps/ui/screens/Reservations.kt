package com.example.aps.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aps.CameraActivity
import com.example.aps.api.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ReservationDisplay(
    val id: Int,
    val plate: String,
    val status: String,
    val statusColor: Color,
    val price: String,
    val customerName: String,
    val spot: String,
    val timeRange: String,
    val onCheckIn: () -> Unit,
    val onCheckOut: () -> Unit
)

@Composable
fun AdminReservationsScreen(navController: NavController) {

    var activeTab by remember { mutableStateOf("reservations") }

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val api = remember {
        RetrofitClient.getClient { sessionManager.getAccessToken() }
            .create(ApiService::class.java)
    }

    var reservations by remember { mutableStateOf<List<ReservationDisplay>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // CAMERA RESULT HANDLER
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult

        val matched = data.getBooleanExtra(CameraActivity.EXTRA_MATCHED, false)
        val reservationId = data.getIntExtra(CameraActivity.EXTRA_RESERVATION_ID, -1)
        val mode = data.getStringExtra(CameraActivity.EXTRA_MODE) ?: "checkin"

        if (!matched || reservationId == -1) return@rememberLauncherForActivityResult

        scope.launch {
            try {
                val body = if (mode == "checkout") {
                    val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        .format(Date())
                    ReservationBase("Settled", nowIso)
                } else {
                    ReservationBase("Active", null)
                }

                val resp = api.updateReservation(reservationId, body)
                if (!resp.isSuccessful) {
                    errorMessage = "Update failed: ${resp.code()}"
                } else {
                    loadReservations(api, sessionManager, context) {
                        reservations = it
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    // FIRST LOAD
    LaunchedEffect(Unit) {
        loadReservations(api, sessionManager, context) {
            reservations = it
            isLoading = false
        }
    }

    Scaffold(
        bottomBar = {
            AdminBottomNavBar(
                activeTab = activeTab,
                navController = navController,
                onTabChange = { activeTab = it }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF6F7F8))
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    "Active Reservations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                when {
                    isLoading -> CircularProgressIndicator()
                    errorMessage != null -> Text(errorMessage!!, color = Color.Red)
                    reservations.isEmpty() -> Text("No reservations found")
                    else -> {
                        reservations.forEach { res ->
                            ReservationCard(res)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }

                Spacer(Modifier.height(70.dp))
            }
        }
    }
}

suspend fun loadReservations(
    api: ApiService,
    sessionManager: SessionManager,
    context: Context,
    onResult: (List<ReservationDisplay>) -> Unit
) {
    try {
        // 1. GET ADMIN
        val adminResp = api.getMyAdmin()
        if (!adminResp.isSuccessful || adminResp.body() == null) {
            onResult(emptyList()); return
        }
        val adminUuid = adminResp.body()!!.uuid

        // 2. GET PARKING
        val parkingResp = api.listParkings()
        if (!parkingResp.isSuccessful || parkingResp.body() == null) {
            onResult(emptyList()); return
        }
        val parking = parkingResp.body()!!
            .firstOrNull { it.owner_uuid == adminUuid }
            ?: parkingResp.body()!!.firstOrNull()

        if (parking == null) {
            onResult(emptyList()); return
        }

        // 3. RESERVATIONS
        val resResp = api.listReservations()
        if (!resResp.isSuccessful || resResp.body() == null) {
            onResult(emptyList()); return
        }
        val reservations = resResp.body()!!
            .filter { it.parking_id == parking.name }

        // 4. PEOPLE
        val peopleResp = api.listPeople()
        val peopleMap = if (peopleResp.isSuccessful && peopleResp.body() != null)
            peopleResp.body()!!.associateBy { it.uuid }
        else emptyMap()

        val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val disp = SimpleDateFormat("HH:mm", Locale.getDefault())

        val result = reservations.map { res ->
            val person = peopleMap[res.people_uuid]
            val plate = person?.plate_number?.toString() ?: "N/A"

            val start = iso.parse(res.time) ?: Date()
            val end = if (res.checkout_time != null)
                (iso.parse(res.checkout_time) ?: Date())
            else Date(start.time + 2 * 60 * 60 * 1000)

            val timeRange = "${disp.format(start)} - ${disp.format(end)}"

            val bubbleColor = when (res.status.lowercase()) {
                "pending" -> Color.Gray
                "active" -> Color(0xFF16C172)
                "settled" -> Color(0xFFFFA500)
                else -> Color.Gray
            }

            ReservationDisplay(
                id = res.id,
                plate = plate,
                status = res.status.lowercase(),
                statusColor = bubbleColor,
                price = "$${res.price}",
                customerName = "Customer $plate",
                spot = "Spot ${res.id}",
                timeRange = timeRange,

                onCheckIn = {
                    val intent = Intent(context, CameraActivity::class.java).apply {
                        putExtra(CameraActivity.EXTRA_EXPECTED_PLATE, plate)
                        putExtra(CameraActivity.EXTRA_RESERVATION_ID, res.id)
                        putExtra(CameraActivity.EXTRA_MODE, "checkin")
                    }
                    if (context is Activity) context.startActivity(intent)
                },

                onCheckOut = {
                    val intent = Intent(context, CameraActivity::class.java).apply {
                        putExtra(CameraActivity.EXTRA_EXPECTED_PLATE, plate)
                        putExtra(CameraActivity.EXTRA_RESERVATION_ID, res.id)
                        putExtra(CameraActivity.EXTRA_MODE, "checkout")
                    }
                    if (context is Activity) context.startActivity(intent)
                }
            )
        }

        onResult(result)

    } catch (e: Exception) {
        onResult(emptyList())
    }
}

@Composable
fun ReservationCard(r: ReservationDisplay) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Row(
                    Modifier.fillMaxWidth(),
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
                            Text("🚗")
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(r.plate, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .background(r.statusColor, RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(r.status, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    Text(r.price, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(r.customerName, fontSize = 17.sp)
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(r.spot, fontSize = 15.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏱")
                    Spacer(Modifier.width(6.dp))
                    Text(r.timeRange, fontSize = 15.sp)

                    Spacer(Modifier.width(12.dp))

                    if (r.status == "pending") {
                        Box(
                            Modifier
                                .background(Color(0xFF16C172), RoundedCornerShape(12.dp))
                                .clickable { r.onCheckIn() }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Check In", color = Color.White)
                        }
                    }

                    if (r.status == "active") {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .background(Color(0xFFFF4444), RoundedCornerShape(12.dp))
                                .clickable { r.onCheckOut() }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Check Out", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
