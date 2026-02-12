package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureSubscription
import com.vainkop.opspocket.domain.model.AzureTenant
import com.vainkop.opspocket.domain.model.AzureVm
import com.vainkop.opspocket.domain.model.VmPowerState
import com.vainkop.opspocket.domain.repository.AzureRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class GetVirtualMachinesUseCaseTest {

    private val sampleVm = AzureVm(
        id = "/subscriptions/sub-1/resourceGroups/rg/providers/Microsoft.Compute/virtualMachines/vm-1",
        name = "vm-1",
        resourceGroup = "rg",
        location = "eastus",
        vmSize = "Standard_B2s",
        provisioningState = "Succeeded",
        powerState = VmPowerState.RUNNING,
    )

    private fun createRepository(
        getVmsResult: AppResult<List<AzureVm>>,
        expectedSubscriptionId: String? = null,
    ): AzureRepository = object : AzureRepository {
        override suspend fun getTenants(): AppResult<List<AzureTenant>> = error("Not expected")
        override suspend fun getSubscriptions(): AppResult<List<AzureSubscription>> = error("Not expected")
        override suspend fun getVirtualMachines(subscriptionId: String): AppResult<List<AzureVm>> {
            if (expectedSubscriptionId != null) {
                assertEquals(expectedSubscriptionId, subscriptionId)
            }
            return getVmsResult
        }
        override suspend fun getVmWithInstanceView(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<AzureVm> = error("Not expected")
        override suspend fun startVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
        override suspend fun stopVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
        override suspend fun deallocateVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
        override suspend fun restartVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
    }

    @Test
    fun `returns Success with VM list from repository`() = runTest {
        val vms = listOf(sampleVm, sampleVm.copy(id = "vm-2-id", name = "vm-2"))
        val repository = createRepository(AppResult.Success(vms))
        val useCase = GetVirtualMachinesUseCase(repository)

        val result = useCase("sub-1")

        assertTrue(result is AppResult.Success)
        assertEquals(vms, (result as AppResult.Success).data)
    }

    @Test
    fun `passes subscriptionId to repository`() = runTest {
        val repository = createRepository(
            AppResult.Success(emptyList()),
            expectedSubscriptionId = "my-sub-id",
        )
        val useCase = GetVirtualMachinesUseCase(repository)

        useCase("my-sub-id")
    }

    @Test
    fun `returns Error from repository`() = runTest {
        val repository = createRepository(AppResult.Error("Not found", 404))
        val useCase = GetVirtualMachinesUseCase(repository)

        val result = useCase("sub-1")

        assertTrue(result is AppResult.Error)
        assertEquals("Not found", (result as AppResult.Error).message)
    }

    @Test
    fun `returns NetworkError from repository`() = runTest {
        val exception = IOException("Connection refused")
        val repository = createRepository(AppResult.NetworkError(exception))
        val useCase = GetVirtualMachinesUseCase(repository)

        val result = useCase("sub-1")

        assertTrue(result is AppResult.NetworkError)
        assertEquals(exception, (result as AppResult.NetworkError).throwable)
    }
}
