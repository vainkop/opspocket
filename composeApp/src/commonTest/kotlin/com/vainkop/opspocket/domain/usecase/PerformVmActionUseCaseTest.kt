package com.vainkop.opspocket.domain.usecase

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureSubscription
import com.vainkop.opspocket.domain.model.AzureTenant
import com.vainkop.opspocket.domain.model.AzureVm
import com.vainkop.opspocket.domain.model.VmAction
import com.vainkop.opspocket.domain.repository.AzureRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PerformVmActionUseCaseTest {

    private var lastCalledMethod: String? = null

    private fun createRepository(): AzureRepository = object : AzureRepository {
        override suspend fun getTenants(): AppResult<List<AzureTenant>> = error("Not expected")
        override suspend fun getSubscriptions(): AppResult<List<AzureSubscription>> = error("Not expected")
        override suspend fun getVirtualMachines(subscriptionId: String): AppResult<List<AzureVm>> = error("Not expected")
        override suspend fun getVmWithInstanceView(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<AzureVm> = error("Not expected")
        override suspend fun startVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> {
            lastCalledMethod = "startVm"
            assertEquals("sub-1", subscriptionId)
            assertEquals("rg-1", resourceGroup)
            assertEquals("vm-1", vmName)
            return AppResult.Success(Unit)
        }
        override suspend fun stopVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> {
            lastCalledMethod = "stopVm"
            return AppResult.Success(Unit)
        }
        override suspend fun deallocateVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> {
            lastCalledMethod = "deallocateVm"
            return AppResult.Success(Unit)
        }
        override suspend fun restartVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> {
            lastCalledMethod = "restartVm"
            return AppResult.Success(Unit)
        }
    }

    @Test
    fun `START action calls startVm`() = runTest {
        lastCalledMethod = null
        val repository = createRepository()
        val useCase = PerformVmActionUseCase(repository)

        val result = useCase(VmAction.START, "sub-1", "rg-1", "vm-1")

        assertTrue(result is AppResult.Success)
        assertEquals("startVm", lastCalledMethod)
    }

    @Test
    fun `STOP action calls stopVm`() = runTest {
        lastCalledMethod = null
        val repository = createRepository()
        val useCase = PerformVmActionUseCase(repository)

        val result = useCase(VmAction.STOP, "sub-1", "rg-1", "vm-1")

        assertTrue(result is AppResult.Success)
        assertEquals("stopVm", lastCalledMethod)
    }

    @Test
    fun `DEALLOCATE action calls deallocateVm`() = runTest {
        lastCalledMethod = null
        val repository = createRepository()
        val useCase = PerformVmActionUseCase(repository)

        val result = useCase(VmAction.DEALLOCATE, "sub-1", "rg-1", "vm-1")

        assertTrue(result is AppResult.Success)
        assertEquals("deallocateVm", lastCalledMethod)
    }

    @Test
    fun `RESTART action calls restartVm`() = runTest {
        lastCalledMethod = null
        val repository = createRepository()
        val useCase = PerformVmActionUseCase(repository)

        val result = useCase(VmAction.RESTART, "sub-1", "rg-1", "vm-1")

        assertTrue(result is AppResult.Success)
        assertEquals("restartVm", lastCalledMethod)
    }
}
