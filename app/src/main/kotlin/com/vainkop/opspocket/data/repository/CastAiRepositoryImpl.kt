package com.vainkop.opspocket.data.repository

import com.vainkop.opspocket.data.mapper.ClusterMapper.toDomain
import com.vainkop.opspocket.data.remote.CastAiApi
import com.vainkop.opspocket.data.remote.dto.RebalancingPlanCreateRequestDto
import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.Cluster
import com.vainkop.opspocket.domain.model.RebalancingPlan
import com.vainkop.opspocket.domain.model.RebalancingResult
import com.vainkop.opspocket.domain.repository.CastAiRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CastAiRepositoryImpl @Inject constructor(
    private val api: CastAiApi,
) : CastAiRepository {

    override suspend fun getClusters(): AppResult<List<Cluster>> {
        return try {
            val response = api.getClusters()
            val clusters = response.items?.map { it.toDomain() } ?: emptyList()
            AppResult.Success(clusters)
        } catch (e: HttpException) {
            AppResult.Error(e.message(), e.code())
        } catch (e: IOException) {
            AppResult.NetworkError(e)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun getClusterDetails(clusterId: String): AppResult<Cluster> {
        return try {
            val response = api.getClusterDetails(clusterId)
            AppResult.Success(response.toDomain())
        } catch (e: HttpException) {
            AppResult.Error(e.message(), e.code())
        } catch (e: IOException) {
            AppResult.NetworkError(e)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun createRebalancingPlan(
        clusterId: String,
        minNodes: Int,
    ): AppResult<RebalancingPlan> {
        return try {
            val request = RebalancingPlanCreateRequestDto(minNodes = minNodes)
            val response = api.createRebalancingPlan(clusterId, request)
            AppResult.Success(RebalancingPlan(rebalancingPlanId = response.rebalancingPlanId))
        } catch (e: HttpException) {
            AppResult.Error(e.message(), e.code())
        } catch (e: IOException) {
            AppResult.NetworkError(e)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun executeRebalancingPlan(
        clusterId: String,
        planId: String,
    ): AppResult<RebalancingResult> {
        return try {
            val response = api.executeRebalancingPlan(clusterId, planId)
            AppResult.Success(
                RebalancingResult(
                    clusterId = response.clusterId.orEmpty(),
                    rebalancingPlanId = response.rebalancingPlanId.orEmpty(),
                    status = response.status.orEmpty(),
                    createdAt = response.createdAt.orEmpty(),
                    updatedAt = response.updatedAt.orEmpty(),
                )
            )
        } catch (e: HttpException) {
            AppResult.Error(e.message(), e.code())
        } catch (e: IOException) {
            AppResult.NetworkError(e)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }
}
