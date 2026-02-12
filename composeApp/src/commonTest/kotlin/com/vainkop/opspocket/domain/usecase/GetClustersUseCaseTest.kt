package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AgentStatus
import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.Cluster
import com.vainkop.opspocket.domain.model.ClusterStatus
import com.vainkop.opspocket.domain.model.RebalancingPlan
import com.vainkop.opspocket.domain.model.RebalancingResult
import com.vainkop.opspocket.domain.repository.CastAiRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetClustersUseCaseTest {

    private val sampleCluster = Cluster(
        id = "c-1",
        name = "test-cluster",
        regionName = "us-east-1",
        regionDisplayName = "US East",
        status = ClusterStatus.READY,
        agentStatus = AgentStatus.ONLINE,
        providerType = "eks",
        createdAt = "2024-01-01T00:00:00Z",
    )

    private fun createRepository(
        getClustersResult: AppResult<List<Cluster>>,
    ): CastAiRepository = object : CastAiRepository {
        override suspend fun getClusters(): AppResult<List<Cluster>> = getClustersResult
        override suspend fun getClusterDetails(clusterId: String): AppResult<Cluster> =
            error("Not expected")
        override suspend fun createRebalancingPlan(clusterId: String, minNodes: Int): AppResult<RebalancingPlan> =
            error("Not expected")
        override suspend fun executeRebalancingPlan(clusterId: String, planId: String): AppResult<RebalancingResult> =
            error("Not expected")
    }

    @Test
    fun `returns Success with cluster list from repository`() = runTest {
        val clusters = listOf(sampleCluster, sampleCluster.copy(id = "c-2", name = "other"))
        val repository = createRepository(AppResult.Success(clusters))
        val useCase = GetClustersUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Success)
        assertEquals(clusters, (result as AppResult.Success).data)
    }

    @Test
    fun `returns Success with empty list`() = runTest {
        val repository = createRepository(AppResult.Success(emptyList()))
        val useCase = GetClustersUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Success)
        assertEquals(emptyList<Cluster>(), (result as AppResult.Success).data)
    }

    @Test
    fun `returns Error from repository`() = runTest {
        val repository = createRepository(AppResult.Error("Server error", 500))
        val useCase = GetClustersUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Error)
        val error = result as AppResult.Error
        assertEquals("Server error", error.message)
        assertEquals(500, error.code)
    }

    @Test
    fun `returns NetworkError from repository`() = runTest {
        val exception = Exception("No network")
        val repository = createRepository(AppResult.NetworkError(exception))
        val useCase = GetClustersUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.NetworkError)
        assertEquals(exception, (result as AppResult.NetworkError).throwable)
    }
}
