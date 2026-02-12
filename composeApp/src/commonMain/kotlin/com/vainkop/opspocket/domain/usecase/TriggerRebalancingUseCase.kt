package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.RebalancingResult
import com.vainkop.opspocket.domain.repository.CastAiRepository
import kotlinx.coroutines.delay

class TriggerRebalancingUseCase(
    private val repository: CastAiRepository,
) {
    suspend operator fun invoke(
        clusterId: String,
        minNodes: Int = 1,
        onPlanCreated: (String) -> Unit = {},
    ): AppResult<RebalancingResult> {
        val planResult = repository.createRebalancingPlan(clusterId, minNodes)
        if (planResult !is AppResult.Success) {
            @Suppress("UNCHECKED_CAST")
            return planResult as AppResult<RebalancingResult>
        }
        val planId = planResult.data.rebalancingPlanId
        onPlanCreated(planId)
        delay(5_000)
        return repository.executeRebalancingPlan(clusterId, planId)
    }
}
