package com.vainkop.opspocket.presentation.clusterdetails

import com.vainkop.opspocket.domain.model.AgentStatus
import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.Cluster
import com.vainkop.opspocket.domain.model.ClusterStatus
import com.vainkop.opspocket.domain.model.RebalancingPlan
import com.vainkop.opspocket.domain.model.RebalancingResult
import com.vainkop.opspocket.domain.repository.CastAiRepository
import com.vainkop.opspocket.domain.usecase.GetClusterDetailsUseCase
import com.vainkop.opspocket.domain.usecase.TriggerRebalancingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ClusterDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val readyCluster = Cluster(
        id = "cluster-1",
        name = "production",
        regionName = "us-east-1",
        regionDisplayName = "US East",
        status = ClusterStatus.READY,
        agentStatus = AgentStatus.ONLINE,
        providerType = "eks",
        createdAt = "2024-01-01T00:00:00Z",
    )

    private val samplePlan = RebalancingPlan(rebalancingPlanId = "plan-xyz")

    private val sampleRebalancingResult = RebalancingResult(
        clusterId = "cluster-1",
        rebalancingPlanId = "plan-xyz",
        status = "executing",
        createdAt = "2024-01-15T10:30:00Z",
        updatedAt = "2024-01-15T10:31:00Z",
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createRepository(
        getClusterDetailsResult: AppResult<Cluster> = AppResult.Success(readyCluster),
        createPlanResult: AppResult<RebalancingPlan> = AppResult.Success(samplePlan),
        executePlanResult: AppResult<RebalancingResult> = AppResult.Success(sampleRebalancingResult),
    ): CastAiRepository = object : CastAiRepository {
        override suspend fun getClusters(): AppResult<List<Cluster>> = error("Not expected")
        override suspend fun getClusterDetails(clusterId: String): AppResult<Cluster> =
            getClusterDetailsResult
        override suspend fun createRebalancingPlan(clusterId: String, minNodes: Int): AppResult<RebalancingPlan> =
            createPlanResult
        override suspend fun executeRebalancingPlan(clusterId: String, planId: String): AppResult<RebalancingResult> =
            executePlanResult
    }

    private fun createViewModel(
        repository: CastAiRepository,
        clusterId: String = "cluster-1",
    ): ClusterDetailsViewModel {
        val getDetailsUseCase = GetClusterDetailsUseCase(repository)
        val triggerRebalancingUseCase = TriggerRebalancingUseCase(repository)
        return ClusterDetailsViewModel(clusterId, getDetailsUseCase, triggerRebalancingUseCase)
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val repository = createRepository()
        val viewModel = createViewModel(repository)

        assertEquals(ClusterDetailsUiState.Loading, viewModel.detailsState.value)
    }

    @Test
    fun `loads cluster details on init`() = runTest {
        val repository = createRepository(getClusterDetailsResult = AppResult.Success(readyCluster))
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.detailsState.value
        assertTrue(state is ClusterDetailsUiState.Success)
        assertEquals(readyCluster, (state as ClusterDetailsUiState.Success).cluster)
    }

    @Test
    fun `state is Error when details load fails`() = runTest {
        val repository = createRepository(
            getClusterDetailsResult = AppResult.Error("Not found", 404),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.detailsState.value
        assertTrue(state is ClusterDetailsUiState.Error)
        assertEquals("Not found", (state as ClusterDetailsUiState.Error).message)
    }

    @Test
    fun `state is Error with network message on NetworkError`() = runTest {
        val repository = createRepository(
            getClusterDetailsResult = AppResult.NetworkError(Exception("Timeout")),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.detailsState.value
        assertTrue(state is ClusterDetailsUiState.Error)
        assertEquals(
            "Network error. Please check your connection.",
            (state as ClusterDetailsUiState.Error).message,
        )
    }

    @Test
    fun `shows blocking dialog for non-ready cluster status`() = runTest {
        val hibernatedCluster = readyCluster.copy(status = ClusterStatus.HIBERNATED)
        val repository = createRepository(
            getClusterDetailsResult = AppResult.Success(hibernatedCluster),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.triggerRebalancing()
        advanceUntilIdle()

        assertEquals("hibernated", viewModel.showBlockingDialog.value)
    }

    @Test
    fun `shows blocking dialog for hibernating status`() = runTest {
        val hibernatingCluster = readyCluster.copy(status = ClusterStatus.HIBERNATING)
        val repository = createRepository(
            getClusterDetailsResult = AppResult.Success(hibernatingCluster),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.triggerRebalancing()
        advanceUntilIdle()

        assertEquals("hibernating", viewModel.showBlockingDialog.value)
    }

    @Test
    fun `shows blocking dialog for warning status`() = runTest {
        val warningCluster = readyCluster.copy(status = ClusterStatus.WARNING)
        val repository = createRepository(
            getClusterDetailsResult = AppResult.Success(warningCluster),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.triggerRebalancing()
        advanceUntilIdle()

        assertEquals("warning", viewModel.showBlockingDialog.value)
    }

    @Test
    fun `dismissBlockingDialog clears dialog state`() = runTest {
        val hibernatedCluster = readyCluster.copy(status = ClusterStatus.HIBERNATED)
        val repository = createRepository(
            getClusterDetailsResult = AppResult.Success(hibernatedCluster),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.triggerRebalancing()
        advanceUntilIdle()
        assertEquals("hibernated", viewModel.showBlockingDialog.value)

        viewModel.dismissBlockingDialog()
        assertNull(viewModel.showBlockingDialog.value)
    }

    @Test
    fun `does not show blocking dialog for ready status`() = runTest {
        val repository = createRepository(
            getClusterDetailsResult = AppResult.Success(readyCluster),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.triggerRebalancing()
        advanceUntilIdle()

        assertNull(viewModel.showBlockingDialog.value)
    }

    @Test
    fun `rebalancing succeeds with correct state transitions`() = runTest {
        val repository = createRepository(
            getClusterDetailsResult = AppResult.Success(readyCluster),
            createPlanResult = AppResult.Success(samplePlan),
            executePlanResult = AppResult.Success(sampleRebalancingResult),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        // Initially Idle
        assertEquals(RebalancingUiState.Idle, viewModel.rebalancingState.value)

        viewModel.triggerRebalancing()
        advanceUntilIdle()

        // After completion, should be Success
        val rebalancingState = viewModel.rebalancingState.value
        assertTrue(
            rebalancingState is RebalancingUiState.Success,
            "Expected Success but got $rebalancingState",
        )
        val message = (rebalancingState as RebalancingUiState.Success).message
        assertTrue(message.contains("plan-xyz"))
        assertTrue(message.contains("executing"))
    }

    @Test
    fun `rebalancing failure on plan creation updates state to Failure`() = runTest {
        val repository = createRepository(
            getClusterDetailsResult = AppResult.Success(readyCluster),
            createPlanResult = AppResult.Error("Plan failed", 400),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.triggerRebalancing()
        advanceUntilIdle()

        val state = viewModel.rebalancingState.value
        assertTrue(state is RebalancingUiState.Failure)
        assertEquals("Plan failed", (state as RebalancingUiState.Failure).message)
    }

    @Test
    fun `rebalancing failure on execution updates state to Failure`() = runTest {
        val repository = createRepository(
            getClusterDetailsResult = AppResult.Success(readyCluster),
            createPlanResult = AppResult.Success(samplePlan),
            executePlanResult = AppResult.Error("Execution failed", 500),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.triggerRebalancing()
        advanceUntilIdle()

        val state = viewModel.rebalancingState.value
        assertTrue(state is RebalancingUiState.Failure)
        assertEquals("Execution failed", (state as RebalancingUiState.Failure).message)
    }

    @Test
    fun `rebalancing network error updates state to Failure`() = runTest {
        val repository = createRepository(
            getClusterDetailsResult = AppResult.Success(readyCluster),
            createPlanResult = AppResult.Success(samplePlan),
            executePlanResult = AppResult.NetworkError(Exception("Network down")),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.triggerRebalancing()
        advanceUntilIdle()

        val state = viewModel.rebalancingState.value
        assertTrue(state is RebalancingUiState.Failure)
        assertEquals(
            "Network error during rebalancing.",
            (state as RebalancingUiState.Failure).message,
        )
    }

    @Test
    fun `resetRebalancingState returns to Idle`() = runTest {
        val repository = createRepository(
            getClusterDetailsResult = AppResult.Success(readyCluster),
            createPlanResult = AppResult.Success(samplePlan),
            executePlanResult = AppResult.Success(sampleRebalancingResult),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.triggerRebalancing()
        advanceUntilIdle()

        assertTrue(viewModel.rebalancingState.value is RebalancingUiState.Success)

        viewModel.resetRebalancingState()

        assertEquals(RebalancingUiState.Idle, viewModel.rebalancingState.value)
    }

    @Test
    fun `triggerRebalancing does nothing when details state is not Success`() = runTest {
        val repository = createRepository(
            getClusterDetailsResult = AppResult.Error("Not found", 404),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.triggerRebalancing()
        advanceUntilIdle()

        // Rebalancing state should remain Idle since details failed
        assertEquals(RebalancingUiState.Idle, viewModel.rebalancingState.value)
    }

    @Test
    fun `initial rebalancing state is Idle`() = runTest {
        val repository = createRepository()
        val viewModel = createViewModel(repository)

        assertEquals(RebalancingUiState.Idle, viewModel.rebalancingState.value)
    }

    @Test
    fun `loadDetails reloads cluster details`() = runTest {
        var callCount = 0
        val repository = object : CastAiRepository {
            override suspend fun getClusters(): AppResult<List<Cluster>> = error("Not expected")
            override suspend fun getClusterDetails(clusterId: String): AppResult<Cluster> {
                callCount++
                return if (callCount == 1) {
                    AppResult.Error("Temporary error", 500)
                } else {
                    AppResult.Success(readyCluster)
                }
            }
            override suspend fun createRebalancingPlan(clusterId: String, minNodes: Int): AppResult<RebalancingPlan> =
                error("Not expected")
            override suspend fun executeRebalancingPlan(clusterId: String, planId: String): AppResult<RebalancingResult> =
                error("Not expected")
        }

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.detailsState.value is ClusterDetailsUiState.Error)

        viewModel.loadDetails()
        advanceUntilIdle()

        assertTrue(viewModel.detailsState.value is ClusterDetailsUiState.Success)
    }
}
