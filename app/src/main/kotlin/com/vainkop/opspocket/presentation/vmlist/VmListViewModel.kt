package com.vainkop.opspocket.presentation.vmlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vainkop.opspocket.data.local.AzurePreferences
import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureVm
import com.vainkop.opspocket.domain.usecase.GetVirtualMachinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VmListUiState {
    data object Loading : VmListUiState()
    data class Success(val vms: List<AzureVm>) : VmListUiState()
    data class Error(val message: String) : VmListUiState()
    data object Empty : VmListUiState()
}

@HiltViewModel
class VmListViewModel @Inject constructor(
    private val getVirtualMachinesUseCase: GetVirtualMachinesUseCase,
    private val azurePreferences: AzurePreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow<VmListUiState>(VmListUiState.Loading)
    val uiState: StateFlow<VmListUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var subscriptionId: String? = null

    init {
        loadVms()
    }

    fun loadVms() {
        viewModelScope.launch {
            if (_uiState.value !is VmListUiState.Loading) {
                _uiState.update { VmListUiState.Loading }
            }
            fetchVms()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.update { true }
            fetchVms()
            _isRefreshing.update { false }
        }
    }

    private suspend fun fetchVms() {
        val subId = subscriptionId ?: azurePreferences.getSelection().subscriptionId
        if (subId == null) {
            _uiState.update { VmListUiState.Error("No subscription selected.") }
            return
        }
        subscriptionId = subId

        when (val result = getVirtualMachinesUseCase(subId)) {
            is AppResult.Success -> {
                if (result.data.isEmpty()) {
                    _uiState.update { VmListUiState.Empty }
                } else {
                    _uiState.update { VmListUiState.Success(result.data) }
                }
            }
            is AppResult.Error -> {
                _uiState.update { VmListUiState.Error(result.message) }
            }
            is AppResult.NetworkError -> {
                _uiState.update {
                    VmListUiState.Error("Network error. Please check your connection.")
                }
            }
        }
    }
}
