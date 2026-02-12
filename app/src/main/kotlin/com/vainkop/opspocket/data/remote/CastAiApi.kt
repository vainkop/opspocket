package com.vainkop.opspocket.data.remote

import com.vainkop.opspocket.data.remote.dto.ClusterDto
import com.vainkop.opspocket.data.remote.dto.ClusterListResponseDto
import com.vainkop.opspocket.data.remote.dto.RebalancingPlanCreateRequestDto
import com.vainkop.opspocket.data.remote.dto.RebalancingPlanCreateResponseDto
import com.vainkop.opspocket.data.remote.dto.RebalancingPlanExecuteResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CastAiApi {

    @GET("v1/kubernetes/external-clusters")
    suspend fun getClusters(): ClusterListResponseDto

    @GET("v1/kubernetes/external-clusters/{clusterId}")
    suspend fun getClusterDetails(@Path("clusterId") clusterId: String): ClusterDto

    @POST("v1/kubernetes/clusters/{clusterId}/rebalancing-plans")
    suspend fun createRebalancingPlan(
        @Path("clusterId") clusterId: String,
        @Body request: RebalancingPlanCreateRequestDto,
    ): RebalancingPlanCreateResponseDto

    @POST("v1/kubernetes/clusters/{clusterId}/rebalancing-plans/{rebalancingPlanId}/execute")
    suspend fun executeRebalancingPlan(
        @Path("clusterId") clusterId: String,
        @Path("rebalancingPlanId") rebalancingPlanId: String,
    ): RebalancingPlanExecuteResponseDto
}
