package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.Cluster
import com.vainkop.opspocket.domain.repository.CastAiRepository
import javax.inject.Inject

class GetClusterDetailsUseCase @Inject constructor(
    private val repository: CastAiRepository,
) {
    suspend operator fun invoke(clusterId: String): AppResult<Cluster> =
        repository.getClusterDetails(clusterId)
}
