package com.vainkop.opspocket.presentation.vmdetails

import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureSubscription
import com.vainkop.opspocket.domain.model.AzureTenant
import com.vainkop.opspocket.domain.model.AzureVm
import com.vainkop.opspocket.domain.model.VmAction
import com.vainkop.opspocket.domain.model.VmPowerState
import com.vainkop.opspocket.domain.repository.AzureRepository
import com.vainkop.opspocket.domain.usecase.PerformVmActionUseCase
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VmDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val sampleVm = AzureVm(
        id = "/subscriptions/sub-1/resourceGroups/rg-1/providers/Microsoft.Compute/virtualMachines/vm-1",
        name = "vm-1",
        resourceGroup = "rg-1",
        location = "eastus",
        vmSize = "Standard_B2s",
        provisioningState = "Succeeded",
        powerState = VmPowerState.RUNNING,
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
        getVmResult: AppResult<AzureVm> = AppResult.Success(sampleVm),
        startVmResult: AppResult<Unit> = AppResult.Success(Unit),
        stopVmResult: AppResult<Unit> = AppResult.Success(Unit),
        deallocateVmResult: AppResult<Unit> = AppResult.Success(Unit),
        restartVmResult: AppResult<Unit> = AppResult.Success(Unit),
    ): AzureRepository = object : AzureRepository {
        override suspend fun getTenants(): AppResult<List<AzureTenant>> = error("Not expected")
        override suspend fun getSubscriptions(): AppResult<List<AzureSubscription>> = error("Not expected")
        override suspend fun getVirtualMachines(subscriptionId: String): AppResult<List<AzureVm>> = error("Not expected")
        override suspend fun getVmWithInstanceView(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<AzureVm> = getVmResult
        override suspend fun startVm(subscriptionId: String, resourceGroup: String, vmName: String) = startVmResult
        override suspend fun stopVm(subscriptionId: String, resourceGroup: String, vmName: String) = stopVmResult
        override suspend fun deallocateVm(subscriptionId: String, resourceGroup: String, vmName: String) = deallocateVmResult
        override suspend fun restartVm(subscriptionId: String, resourceGroup: String, vmName: String) = restartVmResult
    }

    private fun createViewModel(repository: AzureRepository): VmDetailsViewModel {
        val performVmActionUseCase = PerformVmActionUseCase(repository)
        return VmDetailsViewModel("sub-1", "rg-1", "vm-1", repository, performVmActionUseCase)
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val repository = createRepository()
        val viewModel = createViewModel(repository)

        assertEquals(VmDetailsUiState.Loading, viewModel.detailsState.value)
    }

    @Test
    fun `loads VM details on init`() = runTest {
        val repository = createRepository(getVmResult = AppResult.Success(sampleVm))
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.detailsState.value
        assertTrue(state is VmDetailsUiState.Success)
        assertEquals(sampleVm, (state as VmDetailsUiState.Success).vm)
    }

    @Test
    fun `state is Error when details load fails`() = runTest {
        val repository = createRepository(getVmResult = AppResult.Error("Not found", 404))
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.detailsState.value
        assertTrue(state is VmDetailsUiState.Error)
        assertEquals("Not found", (state as VmDetailsUiState.Error).message)
    }

    @Test
    fun `state is Error with network message on NetworkError`() = runTest {
        val repository = createRepository(
            getVmResult = AppResult.NetworkError(Exception("Timeout")),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.detailsState.value
        assertTrue(state is VmDetailsUiState.Error)
        assertEquals(
            "Network error. Please check your connection.",
            (state as VmDetailsUiState.Error).message,
        )
    }

    @Test
    fun `initial action state is Idle`() = runTest {
        val repository = createRepository()
        val viewModel = createViewModel(repository)

        assertEquals(VmActionUiState.Idle, viewModel.actionState.value)
    }

    @Test
    fun `performAction START results in Success`() = runTest {
        val repository = createRepository(
            getVmResult = AppResult.Success(sampleVm),
            startVmResult = AppResult.Success(Unit),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.performAction(VmAction.START)
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is VmActionUiState.Success, "Expected Success but got $state")
        assertTrue((state as VmActionUiState.Success).message.contains("Start"))
    }

    @Test
    fun `performAction STOP results in Success`() = runTest {
        val repository = createRepository(
            getVmResult = AppResult.Success(sampleVm),
            stopVmResult = AppResult.Success(Unit),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.performAction(VmAction.STOP)
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is VmActionUiState.Success)
    }

    @Test
    fun `performAction failure updates state to Failure`() = runTest {
        val repository = createRepository(
            getVmResult = AppResult.Success(sampleVm),
            startVmResult = AppResult.Error("VM locked", 409),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.performAction(VmAction.START)
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is VmActionUiState.Failure)
        assertEquals("VM locked", (state as VmActionUiState.Failure).message)
    }

    @Test
    fun `performAction network error updates state to Failure`() = runTest {
        val repository = createRepository(
            getVmResult = AppResult.Success(sampleVm),
            restartVmResult = AppResult.NetworkError(Exception("Network down")),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.performAction(VmAction.RESTART)
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is VmActionUiState.Failure)
        assertEquals(
            "Network error during VM operation.",
            (state as VmActionUiState.Failure).message,
        )
    }

    @Test
    fun `resetActionState returns to Idle`() = runTest {
        val repository = createRepository(
            getVmResult = AppResult.Success(sampleVm),
            startVmResult = AppResult.Success(Unit),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.performAction(VmAction.START)
        advanceUntilIdle()

        assertTrue(viewModel.actionState.value is VmActionUiState.Success)

        viewModel.resetActionState()

        assertEquals(VmActionUiState.Idle, viewModel.actionState.value)
    }

    @Test
    fun `loadDetails reloads VM details`() = runTest {
        var callCount = 0
        val repository = object : AzureRepository {
            override suspend fun getTenants(): AppResult<List<AzureTenant>> = error("Not expected")
            override suspend fun getSubscriptions(): AppResult<List<AzureSubscription>> = error("Not expected")
            override suspend fun getVirtualMachines(subscriptionId: String): AppResult<List<AzureVm>> = error("Not expected")
            override suspend fun getVmWithInstanceView(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<AzureVm> {
                callCount++
                return if (callCount == 1) {
                    AppResult.Error("Temporary error", 500)
                } else {
                    AppResult.Success(sampleVm)
                }
            }
            override suspend fun startVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
            override suspend fun stopVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
            override suspend fun deallocateVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
            override suspend fun restartVm(subscriptionId: String, resourceGroup: String, vmName: String): AppResult<Unit> = error("Not expected")
        }

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.detailsState.value is VmDetailsUiState.Error)

        viewModel.loadDetails()
        advanceUntilIdle()

        assertTrue(viewModel.detailsState.value is VmDetailsUiState.Success)
    }
}
