package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.Cluster
import com.vainkop.opspocket.domain.model.RebalancingPlan
import com.vainkop.opspocket.domain.model.RebalancingResult
import com.vainkop.opspocket.domain.repository.CastAiRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ValidateApiKeyUseCaseTest {

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
    fun `returns Success when getClusters succeeds`() = runTest {
        val repository = createRepository(AppResult.Success(emptyList()))
        val useCase = ValidateApiKeyUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Success)
        assertEquals(Unit, (result as AppResult.Success).data)
    }

    @Test
    fun `returns specific message for 401 error`() = runTest {
        val repository = createRepository(AppResult.Error("Unauthorized", 401))
        val useCase = ValidateApiKeyUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Error)
        val error = result as AppResult.Error
        assertEquals("Invalid API key. Please check your key and try again.", error.message)
        assertEquals(401, error.code)
    }

    @Test
    fun `returns specific message for 403 error`() = runTest {
        val repository = createRepository(AppResult.Error("Forbidden", 403))
        val useCase = ValidateApiKeyUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Error)
        val error = result as AppResult.Error
        assertEquals("Insufficient permissions. The API key lacks required access.", error.message)
        assertEquals(403, error.code)
    }

    @Test
    fun `passes through other error codes unchanged`() = runTest {
        val repository = createRepository(AppResult.Error("Internal Server Error", 500))
        val useCase = ValidateApiKeyUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Error)
        val error = result as AppResult.Error
        assertEquals("Internal Server Error", error.message)
        assertEquals(500, error.code)
    }

    @Test
    fun `passes through error without code`() = runTest {
        val repository = createRepository(AppResult.Error("Unknown error", null))
        val useCase = ValidateApiKeyUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Error)
        val error = result as AppResult.Error
        assertEquals("Unknown error", error.message)
        assertEquals(null, error.code)
    }

    @Test
    fun `returns NetworkError on network failure`() = runTest {
        val exception = IOException("Connection timed out")
        val repository = createRepository(AppResult.NetworkError(exception))
        val useCase = ValidateApiKeyUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.NetworkError)
        assertEquals(exception, (result as AppResult.NetworkError).throwable)
    }
}
