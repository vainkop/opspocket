package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureSubscription
import com.vainkop.opspocket.domain.repository.AzureRepository

class GetSubscriptionsUseCase(
    private val repository: AzureRepository,
) {
    suspend operator fun invoke(): AppResult<List<AzureSubscription>> = repository.getSubscriptions()
}
