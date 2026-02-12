package com.vainkop.opspocket.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object ApiKey : Screen("api_key")
    data object ClusterList : Screen("cluster_list")
    data object ClusterDetails : Screen("cluster_details/{clusterId}") {
        fun createRoute(clusterId: String): String = "cluster_details/$clusterId"
    }
}
