package com.vainkop.opspocket.data.remote

import com.vainkop.opspocket.data.remote.dto.ClusterDto
import com.vainkop.opspocket.data.remote.dto.ClusterListResponseDto
import com.vainkop.opspocket.data.remote.dto.RebalancingPlanCreateRequestDto
import com.vainkop.opspocket.data.remote.dto.RebalancingPlanCreateResponseDto
import com.vainkop.opspocket.data.remote.dto.RebalancingPlanExecuteResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class CastAiApiClient(private val httpClient: HttpClient) {

    suspend fun getClusters(): ClusterListResponseDto {
        return httpClient.get("v1/kubernetes/external-clusters").body()
    }

    suspend fun getClusterDetails(clusterId: String): ClusterDto {
        return httpClient.get("v1/kubernetes/external-clusters/$clusterId").body()
    }

    suspend fun createRebalancingPlan(
        clusterId: String,
        request: RebalancingPlanCreateRequestDto,
    ): RebalancingPlanCreateResponseDto {
        return httpClient.post("v1/kubernetes/clusters/$clusterId/rebalancing-plans") {
            setBody(request)
        }.body()
    }

    suspend fun executeRebalancingPlan(
        clusterId: String,
        rebalancingPlanId: String,
    ): RebalancingPlanExecuteResponseDto {
        return httpClient.post("v1/kubernetes/clusters/$clusterId/rebalancing-plans/$rebalancingPlanId/execute").body()
    }
}
