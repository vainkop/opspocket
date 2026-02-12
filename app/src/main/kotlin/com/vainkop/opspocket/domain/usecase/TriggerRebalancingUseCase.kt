package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.RebalancingResult
import com.vainkop.opspocket.domain.repository.CastAiRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class TriggerRebalancingUseCase @Inject constructor(
    private val repository: CastAiRepository,
) {
    suspend operator fun invoke(
        clusterId: String,
        minNodes: Int = 1,
        onPlanCreated: (String) -> Unit = {},
    ): AppResult<RebalancingResult> {
        // Step 1: Create rebalancing plan
        val planResult = repository.createRebalancingPlan(clusterId, minNodes)
        if (planResult !is AppResult.Success) {
            @Suppress("UNCHECKED_CAST")
            return planResult as AppResult<RebalancingResult>
        }

        val planId = planResult.data.rebalancingPlanId
        onPlanCreated(planId)

        // Step 2: Wait for plan to be generated (matches Python script behavior)
        delay(5_000)

        // Step 3: Execute the plan
        return repository.executeRebalancingPlan(clusterId, planId)
    }
}
