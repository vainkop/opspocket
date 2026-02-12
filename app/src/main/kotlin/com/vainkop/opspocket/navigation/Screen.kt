package com.vainkop.opspocket.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object ApiKey : Screen("api_key")
    data object ClusterList : Screen("cluster_list")
    data object ClusterDetails : Screen("cluster_details/{clusterId}") {
        fun createRoute(clusterId: String): String = "cluster_details/$clusterId"
    }
    data object AzureAuth : Screen("azure_auth")
    data object AzureSetup : Screen("azure_setup")
    data object VmList : Screen("vm_list")
    data object VmDetails : Screen("vm_details/{subscriptionId}/{resourceGroup}/{vmName}") {
        fun createRoute(subscriptionId: String, resourceGroup: String, vmName: String): String =
            "vm_details/$subscriptionId/$resourceGroup/$vmName"
    }
}
