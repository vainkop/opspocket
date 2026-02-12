package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureTenant
import com.vainkop.opspocket.domain.repository.AzureRepository

class GetTenantsUseCase(
    private val repository: AzureRepository,
) {
    suspend operator fun invoke(): AppResult<List<AzureTenant>> = repository.getTenants()
}
