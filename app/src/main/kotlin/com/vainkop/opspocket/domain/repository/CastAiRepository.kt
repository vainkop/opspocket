package com.vainkop.opspocket.domain.repository

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.Cluster
import com.vainkop.opspocket.domain.model.RebalancingPlan
import com.vainkop.opspocket.domain.model.RebalancingResult

interface CastAiRepository {
    suspend fun getClusters(): AppResult<List<Cluster>>
    suspend fun getClusterDetails(clusterId: String): AppResult<Cluster>
    suspend fun createRebalancingPlan(clusterId: String, minNodes: Int = 1): AppResult<RebalancingPlan>
    suspend fun executeRebalancingPlan(clusterId: String, planId: String): AppResult<RebalancingResult>
}
