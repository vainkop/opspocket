package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.VmAction
import com.vainkop.opspocket.domain.repository.AzureRepository

class PerformVmActionUseCase(
    private val repository: AzureRepository,
) {
    suspend operator fun invoke(
        action: VmAction,
        subscriptionId: String,
        resourceGroup: String,
        vmName: String,
    ): AppResult<Unit> = when (action) {
        VmAction.START -> repository.startVm(subscriptionId, resourceGroup, vmName)
        VmAction.STOP -> repository.stopVm(subscriptionId, resourceGroup, vmName)
        VmAction.DEALLOCATE -> repository.deallocateVm(subscriptionId, resourceGroup, vmName)
        VmAction.RESTART -> repository.restartVm(subscriptionId, resourceGroup, vmName)
    }
}
