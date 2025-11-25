package com.example.aps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.aps.ui.screens.AdminDashboard
import com.example.aps.ui.screens.BookingSuccessScreen
import com.example.aps.ui.screens.ConfirmBookingScreen
import com.example.aps.ui.screens.HomeScreen
import com.example.aps.ui.screens.LoginScreen
import com.example.aps.ui.screens.LoyaltyScreen
import com.example.aps.ui.screens.ProfileScreen
import com.example.aps.ui.screens.PurchaseHistoryScreen
import com.example.aps.ui.screens.SignUpScreen
import com.example.aps.ui.screens.SplashScreen
import com.example.aps.ui.screens.AdminSettingsScreen
import com.example.aps.ui.screens.FinancialReportsScreen
import com.example.aps.ui.screens.AdminReservationsScreen
import com.example.aps.ui.screens.UserDashboard

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
        composable("home") { HomeScreen(navController) } // Login/Welcome screen
        composable("user_dashboard") { UserDashboard(navController) } // User main screen
        composable("signup") { SignUpScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("admin") { AdminDashboard(navController) }
        composable("admin_reservations") { AdminReservationsScreen(navController) }
        composable("admin_settings") { AdminSettingsScreen(navController) }
        composable("financial_reports") { FinancialReportsScreen(navController) }
        composable("loyalty") { LoyaltyScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("purchase_history") { PurchaseHistoryScreen(navController) }
        composable(
            route = "confirm_booking?parkingName={parkingName}&location={location}&pricePerHour={pricePerHour}&currentCapacity={currentCapacity}&maxCapacity={maxCapacity}&openTime={openTime}&closeTime={closeTime}",
            arguments = listOf(
                navArgument("parkingName") { type = NavType.StringType },
                navArgument("location") { type = NavType.StringType },
                navArgument("pricePerHour") { type = NavType.FloatType },
                navArgument("currentCapacity") { type = NavType.IntType },
                navArgument("maxCapacity") { type = NavType.IntType },
                navArgument("openTime") { type = NavType.StringType },
                navArgument("closeTime") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val parkingName = backStackEntry.arguments?.getString("parkingName") ?: ""
            val location = backStackEntry.arguments?.getString("location") ?: ""
            val pricePerHour = backStackEntry.arguments?.getFloat("pricePerHour") ?: 0f
            val currentCapacity = backStackEntry.arguments?.getInt("currentCapacity") ?: 0
            val maxCapacity = backStackEntry.arguments?.getInt("maxCapacity") ?: 0
            val openTime = backStackEntry.arguments?.getString("openTime") ?: ""
            val closeTime = backStackEntry.arguments?.getString("closeTime") ?: ""
            ConfirmBookingScreen(
                navController = navController,
                parkingName = parkingName,
                parkingLocation = location,
                pricePerHour = pricePerHour.toDouble(),
                currentCapacity = currentCapacity,
                maxCapacity = maxCapacity,
                openTime = openTime,
                closeTime = closeTime
            )
        }
        composable(
            route = "booking_success?parkingName={parkingName}&location={location}&duration={duration}&points={points}",
            arguments = listOf(
                navArgument("parkingName") { type = NavType.StringType },
                navArgument("location") { type = NavType.StringType },
                navArgument("duration") { type = NavType.StringType },
                navArgument("points") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val parkingName = backStackEntry.arguments?.getString("parkingName") ?: ""
            val location = backStackEntry.arguments?.getString("location") ?: ""
            val duration = backStackEntry.arguments?.getString("duration") ?: ""
            val points = backStackEntry.arguments?.getInt("points") ?: 0
            BookingSuccessScreen(
                navController = navController,
                parkingName = parkingName,
                parkingLocation = location,
                duration = duration,
                loyaltyPoints = points
            )
        }
    }
}
