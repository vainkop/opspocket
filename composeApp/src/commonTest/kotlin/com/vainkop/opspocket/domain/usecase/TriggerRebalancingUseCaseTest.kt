package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.Cluster
import com.vainkop.opspocket.domain.model.RebalancingPlan
import com.vainkop.opspocket.domain.model.RebalancingResult
import com.vainkop.opspocket.domain.repository.CastAiRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TriggerRebalancingUseCaseTest {

    private val samplePlan = RebalancingPlan(rebalancingPlanId = "plan-abc")

    private val sampleResult = RebalancingResult(
        clusterId = "cluster-1",
        rebalancingPlanId = "plan-abc",
        status = "executing",
        createdAt = "2024-01-15T10:30:00Z",
        updatedAt = "2024-01-15T10:31:00Z",
    )

    private fun createRepository(
        createPlanResult: AppResult<RebalancingPlan>,
        executePlanResult: AppResult<RebalancingResult> = AppResult.Success(sampleResult),
        onCreatePlanCalled: (String, Int) -> Unit = { _, _ -> },
        onExecutePlanCalled: (String, String) -> Unit = { _, _ -> },
    ): CastAiRepository = object : CastAiRepository {
        override suspend fun getClusters(): AppResult<List<Cluster>> = error("Not expected")
        override suspend fun getClusterDetails(clusterId: String): AppResult<Cluster> =
            error("Not expected")
        override suspend fun createRebalancingPlan(clusterId: String, minNodes: Int): AppResult<RebalancingPlan> {
            onCreatePlanCalled(clusterId, minNodes)
            return createPlanResult
        }
        override suspend fun executeRebalancingPlan(clusterId: String, planId: String): AppResult<RebalancingResult> {
            onExecutePlanCalled(clusterId, planId)
            return executePlanResult
        }
    }

    @Test
    fun `returns Success when plan creation and execution both succeed`() = runTest {
        val repository = createRepository(
            createPlanResult = AppResult.Success(samplePlan),
            executePlanResult = AppResult.Success(sampleResult),
        )
        val useCase = TriggerRebalancingUseCase(repository)

        val result = useCase(clusterId = "cluster-1")

        assertTrue(result is AppResult.Success)
        assertEquals(sampleResult, (result as AppResult.Success).data)
    }

    @Test
    fun `returns error when plan creation fails`() = runTest {
        val repository = createRepository(
            createPlanResult = AppResult.Error("Plan creation failed", 400),
        )
        val useCase = TriggerRebalancingUseCase(repository)

        val result = useCase(clusterId = "cluster-1")

        assertTrue(result is AppResult.Error)
        val error = result as AppResult.Error
        assertEquals("Plan creation failed", error.message)
        assertEquals(400, error.code)
    }

    @Test
    fun `returns NetworkError when plan creation has network failure`() = runTest {
        val exception = Exception("No internet")
        val repository = createRepository(
            createPlanResult = AppResult.NetworkError(exception),
        )
        val useCase = TriggerRebalancingUseCase(repository)

        val result = useCase(clusterId = "cluster-1")

        assertTrue(result is AppResult.NetworkError)
        assertEquals(exception, (result as AppResult.NetworkError).throwable)
    }

    @Test
    fun `returns error when execution fails after successful plan creation`() = runTest {
        val repository = createRepository(
            createPlanResult = AppResult.Success(samplePlan),
            executePlanResult = AppResult.Error("Execution failed", 500),
        )
        val useCase = TriggerRebalancingUseCase(repository)

        val result = useCase(clusterId = "cluster-1")

        assertTrue(result is AppResult.Error)
        val error = result as AppResult.Error
        assertEquals("Execution failed", error.message)
        assertEquals(500, error.code)
    }

    @Test
    fun `returns NetworkError when execution has network failure`() = runTest {
        val exception = Exception("Connection reset")
        val repository = createRepository(
            createPlanResult = AppResult.Success(samplePlan),
            executePlanResult = AppResult.NetworkError(exception),
        )
        val useCase = TriggerRebalancingUseCase(repository)

        val result = useCase(clusterId = "cluster-1")

        assertTrue(result is AppResult.NetworkError)
        assertEquals(exception, (result as AppResult.NetworkError).throwable)
    }

    @Test
    fun `calls onPlanCreated with correct plan ID`() = runTest {
        val repository = createRepository(
            createPlanResult = AppResult.Success(samplePlan),
            executePlanResult = AppResult.Success(sampleResult),
        )
        val useCase = TriggerRebalancingUseCase(repository)

        var capturedPlanId: String? = null
        useCase(clusterId = "cluster-1", onPlanCreated = { capturedPlanId = it })

        assertEquals("plan-abc", capturedPlanId)
    }

    @Test
    fun `does not call onPlanCreated when plan creation fails`() = runTest {
        val repository = createRepository(
            createPlanResult = AppResult.Error("Failed", 400),
        )
        val useCase = TriggerRebalancingUseCase(repository)

        var callbackInvoked = false
        useCase(clusterId = "cluster-1", onPlanCreated = { callbackInvoked = true })

        assertEquals(false, callbackInvoked)
    }

    @Test
    fun `delays 5 seconds between plan creation and execution`() = runTest {
        var executeCalledAtTime = -1L
        val repository = createRepository(
            createPlanResult = AppResult.Success(samplePlan),
            executePlanResult = AppResult.Success(sampleResult),
            onExecutePlanCalled = { _, _ -> executeCalledAtTime = currentTime },
        )
        val useCase = TriggerRebalancingUseCase(repository)

        val startTime = currentTime
        useCase(clusterId = "cluster-1")

        assertTrue(
            executeCalledAtTime - startTime >= 5_000,
            "Expected at least 5000ms delay, but was ${executeCalledAtTime - startTime}ms",
        )
    }

    @Test
    fun `passes clusterId and minNodes to createRebalancingPlan`() = runTest {
        var capturedClusterId: String? = null
        var capturedMinNodes: Int? = null
        val repository = createRepository(
            createPlanResult = AppResult.Success(samplePlan),
            executePlanResult = AppResult.Success(sampleResult),
            onCreatePlanCalled = { id, nodes ->
                capturedClusterId = id
                capturedMinNodes = nodes
            },
        )
        val useCase = TriggerRebalancingUseCase(repository)

        useCase(clusterId = "my-cluster", minNodes = 3)

        assertEquals("my-cluster", capturedClusterId)
        assertEquals(3, capturedMinNodes)
    }

    @Test
    fun `passes clusterId and planId to executeRebalancingPlan`() = runTest {
        var capturedClusterId: String? = null
        var capturedPlanId: String? = null
        val repository = createRepository(
            createPlanResult = AppResult.Success(samplePlan),
            executePlanResult = AppResult.Success(sampleResult),
            onExecutePlanCalled = { id, planId ->
                capturedClusterId = id
                capturedPlanId = planId
            },
        )
        val useCase = TriggerRebalancingUseCase(repository)

        useCase(clusterId = "my-cluster")

        assertEquals("my-cluster", capturedClusterId)
        assertEquals("plan-abc", capturedPlanId)
    }

    @Test
    fun `default minNodes is 1`() = runTest {
        var capturedMinNodes: Int? = null
        val repository = createRepository(
            createPlanResult = AppResult.Success(samplePlan),
            executePlanResult = AppResult.Success(sampleResult),
            onCreatePlanCalled = { _, nodes -> capturedMinNodes = nodes },
        )
        val useCase = TriggerRebalancingUseCase(repository)

        useCase(clusterId = "cluster-1")

        assertEquals(1, capturedMinNodes)
    }
}
