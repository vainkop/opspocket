package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureTenant
import com.vainkop.opspocket.domain.repository.AzureRepository
import javax.inject.Inject

class GetTenantsUseCase @Inject constructor(
    private val repository: AzureRepository,
) {
    suspend operator fun invoke(): AppResult<List<AzureTenant>> = repository.getTenants()
}
