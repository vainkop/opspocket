package com.vainkop.opspocket.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vainkop.opspocket.presentation.apikey.ApiKeyScreen
import com.vainkop.opspocket.presentation.clusterdetails.ClusterDetailsScreen
import com.vainkop.opspocket.presentation.clusterlist.ClusterListScreen
import com.vainkop.opspocket.presentation.home.HomeScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCastAi = {
                    navController.navigate(Screen.ApiKey.route)
                }
            )
        }
        composable(Screen.ApiKey.route) {
            ApiKeyScreen(
                onConnected = {
                    navController.navigate(Screen.ClusterList.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                onKeyDeleted = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }
        composable(Screen.ClusterList.route) {
            ClusterListScreen(
                onClusterClick = { clusterId ->
                    navController.navigate(Screen.ClusterDetails.createRoute(clusterId))
                },
                onNavigateBack = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
                onManageApiKey = {
                    navController.navigate(Screen.ApiKey.route)
                }
            )
        }
        composable(
            route = Screen.ClusterDetails.route,
            arguments = listOf(navArgument("clusterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clusterId = backStackEntry.arguments?.getString("clusterId") ?: return@composable
            ClusterDetailsScreen(
                clusterId = clusterId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
