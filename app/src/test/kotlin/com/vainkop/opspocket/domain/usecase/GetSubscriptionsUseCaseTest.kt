package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureSubscription
import com.vainkop.opspocket.domain.model.AzureTenant
import com.vainkop.opspocket.domain.model.AzureVm
import com.vainkop.opspocket.domain.repository.AzureRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class GetSubscriptionsUseCaseTest {

    private val sampleSubscription = AzureSubscription(
        subscriptionId = "sub-1",
        displayName = "Production",
        state = "Enabled",
    )

    private fun createRepository(
        getSubscriptionsResult: AppResult<List<AzureSubscription>>,
    ): AzureRepository = object : AzureRepository {
        override suspend fun getTenants(): AppResult<List<AzureTenant>> = error("Not expected")
        override suspend fun getSubscriptions() = getSubscriptionsResult
        override suspend fun getVirtualMachines(subscriptionId: String): AppResult<List<AzureVm>> = error("Not expected")
        override suspend fun getVmWithInstanceView(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<AzureVm> = error("Not expected")
        override suspend fun startVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
        override suspend fun stopVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
        override suspend fun deallocateVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
        override suspend fun restartVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
    }

    @Test
    fun `returns Success with subscription list from repository`() = runTest {
        val subs = listOf(sampleSubscription, sampleSubscription.copy(subscriptionId = "sub-2"))
        val repository = createRepository(AppResult.Success(subs))
        val useCase = GetSubscriptionsUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Success)
        assertEquals(subs, (result as AppResult.Success).data)
    }

    @Test
    fun `returns Error from repository`() = runTest {
        val repository = createRepository(AppResult.Error("Forbidden", 403))
        val useCase = GetSubscriptionsUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Error)
        assertEquals("Forbidden", (result as AppResult.Error).message)
    }

    @Test
    fun `returns NetworkError from repository`() = runTest {
        val exception = IOException("Timeout")
        val repository = createRepository(AppResult.NetworkError(exception))
        val useCase = GetSubscriptionsUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.NetworkError)
        assertEquals(exception, (result as AppResult.NetworkError).throwable)
    }
}
