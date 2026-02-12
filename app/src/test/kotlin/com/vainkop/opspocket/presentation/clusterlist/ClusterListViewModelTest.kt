package com.vainkop.opspocket.presentation.clusterlist

import com.vainkop.opspocket.domain.model.AgentStatus
import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.Cluster
import com.vainkop.opspocket.domain.model.ClusterStatus
import com.vainkop.opspocket.domain.usecase.GetClustersUseCase
import com.vainkop.opspocket.domain.model.RebalancingPlan
import com.vainkop.opspocket.domain.model.RebalancingResult
import com.vainkop.opspocket.domain.repository.CastAiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClusterListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

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

    private fun createViewModel(result: AppResult<List<Cluster>>): ClusterListViewModel {
        val repository = createRepository(result)
        val useCase = GetClustersUseCase(repository)
        return ClusterListViewModel(useCase)
    }

    @Test
    fun `state is Success with clusters on successful load`() = runTest {
        val clusters = listOf(sampleCluster, sampleCluster.copy(id = "c-2"))
        val viewModel = createViewModel(AppResult.Success(clusters))

        val state = viewModel.uiState.value

        assertTrue(state is ClusterListUiState.Success)
        assertEquals(clusters, (state as ClusterListUiState.Success).clusters)
    }

    @Test
    fun `state is Empty when cluster list is empty`() = runTest {
        val viewModel = createViewModel(AppResult.Success(emptyList()))

        val state = viewModel.uiState.value

        assertTrue(state is ClusterListUiState.Empty)
    }

    @Test
    fun `state is Error on API error`() = runTest {
        val viewModel = createViewModel(AppResult.Error("Bad request", 400))

        val state = viewModel.uiState.value

        assertTrue(state is ClusterListUiState.Error)
        assertEquals("Bad request", (state as ClusterListUiState.Error).message)
    }

    @Test
    fun `state is Error with network message on NetworkError`() = runTest {
        val viewModel = createViewModel(
            AppResult.NetworkError(java.io.IOException("Timeout"))
        )

        val state = viewModel.uiState.value

        assertTrue(state is ClusterListUiState.Error)
        assertEquals(
            "Network error. Please check your connection.",
            (state as ClusterListUiState.Error).message,
        )
    }

    @Test
    fun `refresh sets isRefreshing to false after completion`() = runTest {
        val viewModel = createViewModel(AppResult.Success(listOf(sampleCluster)))

        viewModel.refresh()

        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `refresh updates state with new data`() = runTest {
        var callCount = 0
        val repository = object : CastAiRepository {
            override suspend fun getClusters(): AppResult<List<Cluster>> {
                callCount++
                return if (callCount == 1) {
                    AppResult.Success(listOf(sampleCluster))
                } else {
                    AppResult.Success(listOf(sampleCluster, sampleCluster.copy(id = "c-2")))
                }
            }
            override suspend fun getClusterDetails(clusterId: String): AppResult<Cluster> =
                error("Not expected")
            override suspend fun createRebalancingPlan(clusterId: String, minNodes: Int): AppResult<RebalancingPlan> =
                error("Not expected")
            override suspend fun executeRebalancingPlan(clusterId: String, planId: String): AppResult<RebalancingResult> =
                error("Not expected")
        }

        val useCase = GetClustersUseCase(repository)
        val viewModel = ClusterListViewModel(useCase)

        // After init, should have 1 cluster
        assertTrue(viewModel.uiState.value is ClusterListUiState.Success)
        assertEquals(1, (viewModel.uiState.value as ClusterListUiState.Success).clusters.size)

        // After refresh, should have 2 clusters
        viewModel.refresh()
        assertEquals(2, (viewModel.uiState.value as ClusterListUiState.Success).clusters.size)
    }

    @Test
    fun `loadClusters fetches clusters and updates state`() = runTest {
        var callCount = 0
        val repository = object : CastAiRepository {
            override suspend fun getClusters(): AppResult<List<Cluster>> {
                callCount++
                return if (callCount == 1) {
                    AppResult.Error("Initial error", 500)
                } else {
                    AppResult.Success(listOf(sampleCluster))
                }
            }
            override suspend fun getClusterDetails(clusterId: String): AppResult<Cluster> =
                error("Not expected")
            override suspend fun createRebalancingPlan(clusterId: String, minNodes: Int): AppResult<RebalancingPlan> =
                error("Not expected")
            override suspend fun executeRebalancingPlan(clusterId: String, planId: String): AppResult<RebalancingResult> =
                error("Not expected")
        }

        val useCase = GetClustersUseCase(repository)
        val viewModel = ClusterListViewModel(useCase)

        // After init, state is Error
        assertTrue(viewModel.uiState.value is ClusterListUiState.Error)

        // After loadClusters, state becomes Success
        viewModel.loadClusters()
        assertTrue(viewModel.uiState.value is ClusterListUiState.Success)
    }
}
