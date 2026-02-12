package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureSubscription
import com.vainkop.opspocket.domain.model.AzureTenant
import com.vainkop.opspocket.domain.model.AzureVm
import com.vainkop.opspocket.domain.repository.AzureRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetTenantsUseCaseTest {

    private val sampleTenant = AzureTenant(tenantId = "t-1", displayName = "Test Org")

    private fun createRepository(
        getTenantsResult: AppResult<List<AzureTenant>>,
    ): AzureRepository = object : AzureRepository {
        override suspend fun getTenants() = getTenantsResult
        override suspend fun getSubscriptions(): AppResult<List<AzureSubscription>> = error("Not expected")
        override suspend fun getVirtualMachines(subscriptionId: String): AppResult<List<AzureVm>> = error("Not expected")
        override suspend fun getVmWithInstanceView(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<AzureVm> = error("Not expected")
        override suspend fun startVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
        override suspend fun stopVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
        override suspend fun deallocateVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
        override suspend fun restartVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
    }

    @Test
    fun `returns Success with tenant list from repository`() = runTest {
        val tenants = listOf(sampleTenant, sampleTenant.copy(tenantId = "t-2"))
        val repository = createRepository(AppResult.Success(tenants))
        val useCase = GetTenantsUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Success)
        assertEquals(tenants, (result as AppResult.Success).data)
    }

    @Test
    fun `returns Error from repository`() = runTest {
        val repository = createRepository(AppResult.Error("Unauthorized", 401))
        val useCase = GetTenantsUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Error)
        assertEquals("Unauthorized", (result as AppResult.Error).message)
    }

    @Test
    fun `returns NetworkError from repository`() = runTest {
        val exception = Exception("No network")
        val repository = createRepository(AppResult.NetworkError(exception))
        val useCase = GetTenantsUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.NetworkError)
        assertEquals(exception, (result as AppResult.NetworkError).throwable)
    }
}
