package com.vainkop.opspocket.presentation.vmdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureVm
import com.vainkop.opspocket.domain.model.VmAction
import com.vainkop.opspocket.domain.repository.AzureRepository
import com.vainkop.opspocket.domain.usecase.PerformVmActionUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class VmDetailsUiState {
    data object Loading : VmDetailsUiState()
    data class Success(val vm: AzureVm) : VmDetailsUiState()
    data class Error(val message: String) : VmDetailsUiState()
}

sealed class VmActionUiState {
    data object Idle : VmActionUiState()
    data class Executing(val action: VmAction) : VmActionUiState()
    data class Success(val message: String) : VmActionUiState()
    data class Failure(val message: String) : VmActionUiState()
}

class VmDetailsViewModel(
    private val subscriptionId: String,
    private val resourceGroup: String,
    private val vmName: String,
    private val repository: AzureRepository,
    private val performVmActionUseCase: PerformVmActionUseCase,
) : ViewModel() {

    private val _detailsState = MutableStateFlow<VmDetailsUiState>(VmDetailsUiState.Loading)
    val detailsState: StateFlow<VmDetailsUiState> = _detailsState.asStateFlow()

    private val _actionState = MutableStateFlow<VmActionUiState>(VmActionUiState.Idle)
    val actionState: StateFlow<VmActionUiState> = _actionState.asStateFlow()

    init {
        loadDetails()
    }

    fun loadDetails() {
        viewModelScope.launch {
            _detailsState.update { VmDetailsUiState.Loading }
            when (val result = repository.getVmWithInstanceView(subscriptionId, resourceGroup, vmName)) {
                is AppResult.Success -> {
                    _detailsState.update { VmDetailsUiState.Success(result.data) }
                }
                is AppResult.Error -> {
                    _detailsState.update { VmDetailsUiState.Error(result.message) }
                }
                is AppResult.NetworkError -> {
                    _detailsState.update {
                        VmDetailsUiState.Error("Network error. Please check your connection.")
                    }
                }
            }
        }
    }

    fun performAction(action: VmAction) {
        viewModelScope.launch {
            _actionState.update { VmActionUiState.Executing(action) }

            when (val result = performVmActionUseCase(action, subscriptionId, resourceGroup, vmName)) {
                is AppResult.Success -> {
                    val actionName = action.name.lowercase().replaceFirstChar { it.uppercase() }
                    _actionState.update {
                        VmActionUiState.Success("$actionName command sent successfully.")
                    }
                    // Wait a moment then refresh to get updated power state
                    delay(2_000)
                    refreshDetails()
                }
                is AppResult.Error -> {
                    _actionState.update { VmActionUiState.Failure(result.message) }
                }
                is AppResult.NetworkError -> {
                    _actionState.update {
                        VmActionUiState.Failure("Network error during VM operation.")
                    }
                }
            }
        }
    }

    fun resetActionState() {
        _actionState.update { VmActionUiState.Idle }
    }

    private suspend fun refreshDetails() {
        when (val result = repository.getVmWithInstanceView(subscriptionId, resourceGroup, vmName)) {
            is AppResult.Success -> {
                _detailsState.update { VmDetailsUiState.Success(result.data) }
            }
            else -> { /* Keep current state on refresh failure */ }
        }
    }
}
