package com.example.aps.ui.screens

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
    val timeRange: String
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
    var rawReservations by remember { mutableStateOf<List<ReservationRead>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // CAMERA RESULT HANDLER
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val data = result.data ?: return@rememberLauncherForActivityResult

        val matched = data.getBooleanExtra(CameraActivity.EXTRA_MATCHED, false)
        val reservationId = data.getIntExtra(CameraActivity.EXTRA_RESERVATION_ID, -1)
        val mode = data.getStringExtra(CameraActivity.EXTRA_MODE) ?: "checkin"

        if (!matched || reservationId == -1) return@rememberLauncherForActivityResult

        scope.launch {
            try {
                // Find original reservation
                val entity = rawReservations.firstOrNull { it.id == reservationId }
                    ?: return@launch

                val updatedStatus = when (mode.lowercase()) {
                    "checkout" -> "Settled"
                    else -> "Active"
                }

                val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val updatedCheckoutTime: String? = when (mode.lowercase()) {
                    "checkout" -> iso.format(Date())
                    else -> entity.checkout_time
                }

                // Build PATCH body (partial update, but we send full for safety)
                val body = ReservationBase(
                    parking_id = entity.parking_id,
                    time = entity.time,
                    status = updatedStatus,
                    checkout_time = updatedCheckoutTime,
                    price = entity.price
                )

                val resp = api.updateReservation(reservationId, body)
                if (!resp.isSuccessful) {
                    errorMessage = "Update failed: ${resp.code()}"
                } else {
                    loadReservations(api, sessionManager, context) { displayList, rawList ->
                        reservations = displayList
                        rawReservations = rawList
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error"
            }
        }
    }

    // FIRST LOAD
    LaunchedEffect(Unit) {
        loadReservations(api, sessionManager, context) { displayList, rawList ->
            reservations = displayList
            rawReservations = rawList
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
                            ReservationCard(
                                r = res,
                                onCheckIn = {
                                    val intent = Intent(context, CameraActivity::class.java).apply {
                                        putExtra(CameraActivity.EXTRA_EXPECTED_PLATE, res.plate)
                                        putExtra(CameraActivity.EXTRA_RESERVATION_ID, res.id)
                                        putExtra(CameraActivity.EXTRA_MODE, "checkin")
                                    }
                                    cameraLauncher.launch(intent)
                                },
                                onCheckOut = {
                                    val intent = Intent(context, CameraActivity::class.java).apply {
                                        putExtra(CameraActivity.EXTRA_EXPECTED_PLATE, res.plate)
                                        putExtra(CameraActivity.EXTRA_RESERVATION_ID, res.id)
                                        putExtra(CameraActivity.EXTRA_MODE, "checkout")
                                    }
                                    cameraLauncher.launch(intent)
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }

                Spacer(Modifier.height(70.dp))
            }
        }
    }
}

/**
 * Loads reservations + related people info, returns:
 *  - display list for UI
 *  - raw list (ReservationRead) for PATCH body building
 */
suspend fun loadReservations(
    api: ApiService,
    sessionManager: SessionManager,
    context: Context,
    onResult: (List<ReservationDisplay>, List<ReservationRead>) -> Unit
) {
    try {
        // 1. GET ADMIN
        val adminResp = api.getMyAdmin()
        if (!adminResp.isSuccessful || adminResp.body() == null) {
            onResult(emptyList(), emptyList()); return
        }
        val adminUuid = adminResp.body()!!.uuid

        // 2. GET PARKING
        val parkingResp = api.listParkings()
        if (!parkingResp.isSuccessful || parkingResp.body() == null) {
            onResult(emptyList(), emptyList()); return
        }
        val parking = parkingResp.body()!!
            .firstOrNull { it.owner_uuid == adminUuid }
            ?: parkingResp.body()!!.firstOrNull()

        if (parking == null) {
            onResult(emptyList(), emptyList()); return
        }

        // 3. RESERVATIONS
        val resResp = api.listReservations()
        if (!resResp.isSuccessful || resResp.body() == null) {
            onResult(emptyList(), emptyList()); return
        }
        val reservations = resResp.body()!!
            .filter { it.parking_id == parking.name }

        // 4. PEOPLE
        val peopleResp = api.listPeople()
        val peopleMap = if (peopleResp.isSuccessful && peopleResp.body() != null)
            peopleResp.body()!!.associateBy { it.uuid }
        else emptyMap()

        val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val dispFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

        val displayList = reservations.map { res ->
            val person = peopleMap[res.people_uuid]
            val plate = person?.plate_number?.toString() ?: "N/A"

            val startDate: Date = try {
                iso.parse(res.time)
            } catch (_: Exception) {
                Date()
            }

            val endDate: Date = try {
                if (res.checkout_time != null)
                    iso.parse(res.checkout_time)
                else
                    Date(startDate.time + 2 * 60 * 60 * 1000)
            } catch (_: Exception) {
                Date(startDate.time + 2 * 60 * 60 * 1000)
            }

            val timeRange = "${dispFmt.format(startDate)} - ${dispFmt.format(endDate)}"

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
                timeRange = timeRange
            )
        }

        onResult(displayList, reservations)

    } catch (e: Exception) {
        onResult(emptyList(), emptyList())
    }
}

@Composable
fun ReservationCard(
    r: ReservationDisplay,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit
) {
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
                                .clickable { onCheckIn() }
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
                                .clickable { onCheckOut() }
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
