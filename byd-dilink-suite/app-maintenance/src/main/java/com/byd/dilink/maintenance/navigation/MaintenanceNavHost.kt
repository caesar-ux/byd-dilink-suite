package com.byd.dilink.maintenance.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.byd.dilink.maintenance.ui.AddCategoryScreen
import com.byd.dilink.maintenance.ui.AddServiceScreen
import com.byd.dilink.maintenance.ui.CategoryDetailScreen
import com.byd.dilink.maintenance.ui.OverviewScreen
import com.byd.dilink.maintenance.ui.VehicleProfileScreen

object MaintenanceRoutes {
    const val OVERVIEW = "overview"
    const val CATEGORY_DETAIL = "category_detail/{id}"
    const val ADD_SERVICE = "add_service"
    const val VEHICLE_PROFILE = "vehicle_profile"
    const val ADD_CATEGORY = "add_category"

    fun categoryDetail(id: Long) = "category_detail/$id"
    fun addServiceForCategory(categoryId: Long) = "add_service?categoryId=$categoryId"
}

@Composable
fun MaintenanceNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MaintenanceRoutes.OVERVIEW
    ) {
        composable(MaintenanceRoutes.OVERVIEW) {
            OverviewScreen(
                onNavigateToCategoryDetail = { id ->
                    navController.navigate(MaintenanceRoutes.categoryDetail(id))
                },
                onNavigateToVehicleProfile = {
                    navController.navigate(MaintenanceRoutes.VEHICLE_PROFILE)
                },
                onNavigateToAddCategory = {
                    navController.navigate(MaintenanceRoutes.ADD_CATEGORY)
                }
            )
        }

        composable(
            route = MaintenanceRoutes.CATEGORY_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("id") ?: return@composable
            CategoryDetailScreen(
                categoryId = categoryId,
                onBack = { navController.popBackStack() },
                onNavigateToAddService = {
                    navController.navigate(MaintenanceRoutes.addServiceForCategory(categoryId))
                }
            )
        }

        composable(
            route = "add_service?categoryId={categoryId}",
            arguments = listOf(
                navArgument("categoryId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val preselectedCategoryId = backStackEntry.arguments?.getLong("categoryId") ?: -1L
            AddServiceScreen(
                preselectedCategoryId = if (preselectedCategoryId == -1L) null else preselectedCategoryId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(MaintenanceRoutes.VEHICLE_PROFILE) {
            VehicleProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(MaintenanceRoutes.ADD_CATEGORY) {
            AddCategoryScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}
