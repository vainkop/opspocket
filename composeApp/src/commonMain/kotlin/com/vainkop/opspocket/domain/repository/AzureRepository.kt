package com.vainkop.opspocket.domain.repository

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureSubscription
import com.vainkop.opspocket.domain.model.AzureTenant
import com.vainkop.opspocket.domain.model.AzureVm

interface AzureRepository {
    suspend fun getTenants(): AppResult<List<AzureTenant>>
    suspend fun getSubscriptions(): AppResult<List<AzureSubscription>>
    suspend fun getVirtualMachines(subscriptionId: String): AppResult<List<AzureVm>>
    suspend fun getVmWithInstanceView(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<AzureVm>
    suspend fun startVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit>
    suspend fun stopVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit>
    suspend fun deallocateVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit>
    suspend fun restartVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit>
}
