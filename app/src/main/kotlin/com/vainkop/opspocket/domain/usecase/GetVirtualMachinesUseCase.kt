package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureVm
import com.vainkop.opspocket.domain.repository.AzureRepository
import javax.inject.Inject

class GetVirtualMachinesUseCase @Inject constructor(
    private val repository: AzureRepository,
) {
    suspend operator fun invoke(subscriptionId: String): AppResult<List<AzureVm>> =
        repository.getVirtualMachines(subscriptionId)
}
