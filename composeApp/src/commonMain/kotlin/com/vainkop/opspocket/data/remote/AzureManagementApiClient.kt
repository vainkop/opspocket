package com.vainkop.opspocket.data.remote

import com.vainkop.opspocket.data.remote.dto.AzureSubscriptionListResponseDto
import com.vainkop.opspocket.data.remote.dto.AzureTenantListResponseDto
import com.vainkop.opspocket.data.remote.dto.AzureVmDto
import com.vainkop.opspocket.data.remote.dto.AzureVmListResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post

class AzureManagementApiClient(private val httpClient: HttpClient) {

    suspend fun getTenants(apiVersion: String = "2020-01-01"): AzureTenantListResponseDto {
        return httpClient.get("tenants") {
            parameter("api-version", apiVersion)
        }.body()
    }

    suspend fun getSubscriptions(apiVersion: String = "2022-12-01"): AzureSubscriptionListResponseDto {
        return httpClient.get("subscriptions") {
            parameter("api-version", apiVersion)
        }.body()
    }

    suspend fun getVirtualMachines(
        subscriptionId: String,
        apiVersion: String = "2024-07-01",
    ): AzureVmListResponseDto {
        return httpClient.get("subscriptions/$subscriptionId/providers/Microsoft.Compute/virtualMachines") {
            parameter("api-version", apiVersion)
        }.body()
    }

    suspend fun getVmWithInstanceView(
        subscriptionId: String,
        resourceGroup: String,
        vmName: String,
        apiVersion: String = "2024-07-01",
    ): AzureVmDto {
        return httpClient.get("subscriptions/$subscriptionId/resourceGroups/$resourceGroup/providers/Microsoft.Compute/virtualMachines/$vmName") {
            parameter("\$expand", "instanceView")
            parameter("api-version", apiVersion)
        }.body()
    }

    suspend fun startVm(
        subscriptionId: String,
        resourceGroup: String,
        vmName: String,
        apiVersion: String = "2024-07-01",
    ) {
        httpClient.post("subscriptions/$subscriptionId/resourceGroups/$resourceGroup/providers/Microsoft.Compute/virtualMachines/$vmName/start") {
            parameter("api-version", apiVersion)
        }
    }

    suspend fun stopVm(
        subscriptionId: String,
        resourceGroup: String,
        vmName: String,
        apiVersion: String = "2024-07-01",
    ) {
        httpClient.post("subscriptions/$subscriptionId/resourceGroups/$resourceGroup/providers/Microsoft.Compute/virtualMachines/$vmName/powerOff") {
            parameter("api-version", apiVersion)
        }
    }

    suspend fun deallocateVm(
        subscriptionId: String,
        resourceGroup: String,
        vmName: String,
        apiVersion: String = "2024-07-01",
    ) {
        httpClient.post("subscriptions/$subscriptionId/resourceGroups/$resourceGroup/providers/Microsoft.Compute/virtualMachines/$vmName/deallocate") {
            parameter("api-version", apiVersion)
        }
    }

    suspend fun restartVm(
        subscriptionId: String,
        resourceGroup: String,
        vmName: String,
        apiVersion: String = "2024-07-01",
    ) {
        httpClient.post("subscriptions/$subscriptionId/resourceGroups/$resourceGroup/providers/Microsoft.Compute/virtualMachines/$vmName/restart") {
            parameter("api-version", apiVersion)
        }
    }
}
