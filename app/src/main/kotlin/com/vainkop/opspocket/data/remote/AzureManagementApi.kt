package com.vainkop.opspocket.data.remote

import com.vainkop.opspocket.data.remote.dto.AzureSubscriptionListResponseDto
import com.vainkop.opspocket.data.remote.dto.AzureTenantListResponseDto
import com.vainkop.opspocket.data.remote.dto.AzureVmDto
import com.vainkop.opspocket.data.remote.dto.AzureVmListResponseDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AzureManagementApi {

    @GET("tenants")
    suspend fun getTenants(
        @Query("api-version") apiVersion: String = "2020-01-01",
    ): AzureTenantListResponseDto

    @GET("subscriptions")
    suspend fun getSubscriptions(
        @Query("api-version") apiVersion: String = "2022-12-01",
    ): AzureSubscriptionListResponseDto

    @GET("subscriptions/{subscriptionId}/providers/Microsoft.Compute/virtualMachines")
    suspend fun getVirtualMachines(
        @Path("subscriptionId") subscriptionId: String,
        @Query("api-version") apiVersion: String = "2024-07-01",
        @Query("\$statusOnly") statusOnly: String = "true",
    ): AzureVmListResponseDto

    @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.Compute/virtualMachines/{vmName}")
    suspend fun getVmWithInstanceView(
        @Path("subscriptionId") subscriptionId: String,
        @Path("resourceGroup") resourceGroup: String,
        @Path("vmName") vmName: String,
        @Query("\$expand") expand: String = "instanceView",
        @Query("api-version") apiVersion: String = "2024-07-01",
    ): AzureVmDto

    @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.Compute/virtualMachines/{vmName}/start")
    suspend fun startVm(
        @Path("subscriptionId") subscriptionId: String,
        @Path("resourceGroup") resourceGroup: String,
        @Path("vmName") vmName: String,
        @Query("api-version") apiVersion: String = "2024-07-01",
    )

    @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.Compute/virtualMachines/{vmName}/powerOff")
    suspend fun stopVm(
        @Path("subscriptionId") subscriptionId: String,
        @Path("resourceGroup") resourceGroup: String,
        @Path("vmName") vmName: String,
        @Query("api-version") apiVersion: String = "2024-07-01",
    )

    @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.Compute/virtualMachines/{vmName}/deallocate")
    suspend fun deallocateVm(
        @Path("subscriptionId") subscriptionId: String,
        @Path("resourceGroup") resourceGroup: String,
        @Path("vmName") vmName: String,
        @Query("api-version") apiVersion: String = "2024-07-01",
    )

    @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.Compute/virtualMachines/{vmName}/restart")
    suspend fun restartVm(
        @Path("subscriptionId") subscriptionId: String,
        @Path("resourceGroup") resourceGroup: String,
        @Path("vmName") vmName: String,
        @Query("api-version") apiVersion: String = "2024-07-01",
    )
}
