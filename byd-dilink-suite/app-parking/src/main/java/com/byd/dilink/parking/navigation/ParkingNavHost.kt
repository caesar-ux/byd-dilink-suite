package com.byd.dilink.parking.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.byd.dilink.parking.ui.ActiveParkingScreen
import com.byd.dilink.parking.ui.FavoritesScreen
import com.byd.dilink.parking.ui.HistoryScreen
import com.byd.dilink.parking.ui.MainScreen

object ParkingRoutes {
    const val MAIN = "main"
    const val ACTIVE_PARKING = "active_parking"
    const val FAVORITES = "favorites"
    const val HISTORY = "history"
}

@Composable
fun ParkingNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = ParkingRoutes.MAIN,
        modifier = modifier
    ) {
        composable(ParkingRoutes.MAIN) {
            MainScreen(
                onNavigateToActive = { navController.navigate(ParkingRoutes.ACTIVE_PARKING) },
                onNavigateToFavorites = { navController.navigate(ParkingRoutes.FAVORITES) },
                onNavigateToHistory = { navController.navigate(ParkingRoutes.HISTORY) }
            )
        }
        composable(ParkingRoutes.ACTIVE_PARKING) {
            ActiveParkingScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(ParkingRoutes.FAVORITES) {
            FavoritesScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(ParkingRoutes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
