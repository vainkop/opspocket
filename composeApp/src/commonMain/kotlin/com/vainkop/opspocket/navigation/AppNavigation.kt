package com.vainkop.opspocket.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vainkop.opspocket.presentation.apikey.ApiKeyScreen
import com.vainkop.opspocket.presentation.azureauth.AzureAuthScreen
import com.vainkop.opspocket.presentation.azuresetup.AzureSetupScreen
import com.vainkop.opspocket.presentation.clusterdetails.ClusterDetailsScreen
import com.vainkop.opspocket.presentation.clusterlist.ClusterListScreen
import com.vainkop.opspocket.presentation.home.HomeScreen
import com.vainkop.opspocket.presentation.vmdetails.VmDetailsScreen
import com.vainkop.opspocket.presentation.vmlist.VmListScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCastAi = {
                    navController.navigate(Screen.ApiKey.route)
                },
                onNavigateToAzure = {
                    navController.navigate(Screen.AzureAuth.route)
                },
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
                },
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
                },
            )
        }
        composable(
            route = Screen.ClusterDetails.route,
            arguments = listOf(navArgument("clusterId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val clusterId = backStackEntry.arguments?.getString("clusterId") ?: return@composable
            ClusterDetailsScreen(
                clusterId = clusterId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.AzureAuth.route) {
            AzureAuthScreen(
                onSignedIn = {
                    navController.navigate(Screen.AzureSetup.createRoute(forceSetup = false)) {
                        popUpTo(Screen.AzureAuth.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Screen.AzureSetup.route,
            arguments = listOf(
                navArgument("forceSetup") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { backStackEntry ->
            val forceSetup = backStackEntry.arguments?.getBoolean("forceSetup") ?: false
            AzureSetupScreen(
                forceSetup = forceSetup,
                onSetupComplete = {
                    navController.navigate(Screen.VmList.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
        composable(Screen.VmList.route) {
            VmListScreen(
                onVmClick = { subscriptionId, resourceGroup, vmName ->
                    navController.navigate(
                        Screen.VmDetails.createRoute(subscriptionId, resourceGroup, vmName),
                    )
                },
                onNavigateBack = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
                onSettings = {
                    navController.navigate(Screen.AzureSetup.createRoute(forceSetup = true))
                },
            )
        }
        composable(
            route = Screen.VmDetails.route,
            arguments = listOf(
                navArgument("subscriptionId") { type = NavType.StringType },
                navArgument("resourceGroup") { type = NavType.StringType },
                navArgument("vmName") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getString("subscriptionId") ?: return@composable
            val resourceGroup = backStackEntry.arguments?.getString("resourceGroup") ?: return@composable
            val vmName = backStackEntry.arguments?.getString("vmName") ?: return@composable
            VmDetailsScreen(
                subscriptionId = subscriptionId,
                resourceGroup = resourceGroup,
                vmName = vmName,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
