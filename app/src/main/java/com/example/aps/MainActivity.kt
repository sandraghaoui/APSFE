package com.example.aps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
        composable("signup") { SignUpScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("admin") { AdminDashboard(navController) }
        composable("admin_reservations") { AdminReservationsScreen(navController) }
        composable("admin_settings") { AdminSettingsScreen(navController) }
        composable("financial_reports") { FinancialReportsScreen(navController) }
        composable("loyalty") { LoyaltyScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("purchase_history") { PurchaseHistoryScreen(navController) }
        composable("confirm_booking") { ConfirmBookingScreen(navController) }
        composable("booking_success") { BookingSuccessScreen(navController) }
    }
}
