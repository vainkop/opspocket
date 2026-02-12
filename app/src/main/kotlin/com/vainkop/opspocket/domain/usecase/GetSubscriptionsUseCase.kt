package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureSubscription
import com.vainkop.opspocket.domain.repository.AzureRepository
import javax.inject.Inject

class GetSubscriptionsUseCase @Inject constructor(
    private val repository: AzureRepository,
) {
    suspend operator fun invoke(): AppResult<List<AzureSubscription>> = repository.getSubscriptions()
}
