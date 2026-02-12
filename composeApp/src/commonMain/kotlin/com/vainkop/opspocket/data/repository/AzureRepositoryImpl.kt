package com.vainkop.opspocket.data.repository

import com.vainkop.opspocket.data.mapper.AzureMapper.toDomain
import com.vainkop.opspocket.data.remote.AzureManagementApiClient
import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureSubscription
import com.vainkop.opspocket.domain.model.AzureTenant
import com.vainkop.opspocket.domain.model.AzureVm
import com.vainkop.opspocket.domain.repository.AzureRepository
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class AzureRepositoryImpl(
    private val api: AzureManagementApiClient,
) : AzureRepository {

    override suspend fun getTenants(): AppResult<List<AzureTenant>> {
        return try {
            val response = api.getTenants()
            val tenants = response.value?.map { it.toDomain() } ?: emptyList()
            AppResult.Success(tenants)
        } catch (e: ClientRequestException) {
            AppResult.Error(e.message, e.response.status.value)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun getSubscriptions(): AppResult<List<AzureSubscription>> {
        return try {
            val response = api.getSubscriptions()
            val subscriptions = response.value?.map { it.toDomain() } ?: emptyList()
            AppResult.Success(subscriptions)
        } catch (e: ClientRequestException) {
            AppResult.Error(e.message, e.response.status.value)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun getVirtualMachines(subscriptionId: String): AppResult<List<AzureVm>> {
        return try {
            val response = api.getVirtualMachines(subscriptionId)
            val vms = response.value?.map { it.toDomain() } ?: emptyList()
            val vmsWithState = coroutineScope {
                vms.map { vm ->
                    async {
                        try {
                            val detail = api.getVmWithInstanceView(
                                subscriptionId, vm.resourceGroup, vm.name,
                            )
                            detail.toDomain()
                        } catch (_: Exception) {
                            vm
                        }
                    }
                }.awaitAll()
            }
            AppResult.Success(vmsWithState)
        } catch (e: ClientRequestException) {
            AppResult.Error(e.message, e.response.status.value)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun getVmWithInstanceView(
        subscriptionId: String,
        resourceGroup: String,
        vmName: String,
    ): AppResult<AzureVm> {
        return try {
            val response = api.getVmWithInstanceView(subscriptionId, resourceGroup, vmName)
            AppResult.Success(response.toDomain())
        } catch (e: ClientRequestException) {
            AppResult.Error(e.message, e.response.status.value)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun startVm(
        subscriptionId: String,
        resourceGroup: String,
        vmName: String,
    ): AppResult<Unit> {
        return try {
            api.startVm(subscriptionId, resourceGroup, vmName)
            AppResult.Success(Unit)
        } catch (e: ClientRequestException) {
            AppResult.Error(e.message, e.response.status.value)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun stopVm(
        subscriptionId: String,
        resourceGroup: String,
        vmName: String,
    ): AppResult<Unit> {
        return try {
            api.stopVm(subscriptionId, resourceGroup, vmName)
            AppResult.Success(Unit)
        } catch (e: ClientRequestException) {
            AppResult.Error(e.message, e.response.status.value)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun deallocateVm(
        subscriptionId: String,
        resourceGroup: String,
        vmName: String,
    ): AppResult<Unit> {
        return try {
            api.deallocateVm(subscriptionId, resourceGroup, vmName)
            AppResult.Success(Unit)
        } catch (e: ClientRequestException) {
            AppResult.Error(e.message, e.response.status.value)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun restartVm(
        subscriptionId: String,
        resourceGroup: String,
        vmName: String,
    ): AppResult<Unit> {
        return try {
            api.restartVm(subscriptionId, resourceGroup, vmName)
            AppResult.Success(Unit)
        } catch (e: ClientRequestException) {
            AppResult.Error(e.message, e.response.status.value)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }
}
