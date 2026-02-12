package com.vainkop.opspocket.presentation.clusterdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.Cluster
import com.vainkop.opspocket.domain.model.ClusterStatus
import com.vainkop.opspocket.domain.usecase.GetClusterDetailsUseCase
import com.vainkop.opspocket.domain.usecase.TriggerRebalancingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ClusterDetailsUiState {
    data object Loading : ClusterDetailsUiState()
    data class Success(val cluster: Cluster) : ClusterDetailsUiState()
    data class Error(val message: String) : ClusterDetailsUiState()
}

sealed class RebalancingUiState {
    data object Idle : RebalancingUiState()
    data object CreatingPlan : RebalancingUiState()
    data class WaitingToExecute(val planId: String) : RebalancingUiState()
    data class Executing(val planId: String) : RebalancingUiState()
    data class Success(val message: String) : RebalancingUiState()
    data class Failure(val message: String) : RebalancingUiState()
}

class ClusterDetailsViewModel(
    private val clusterId: String,
    private val getClusterDetailsUseCase: GetClusterDetailsUseCase,
    private val triggerRebalancingUseCase: TriggerRebalancingUseCase,
) : ViewModel() {

    private val _detailsState = MutableStateFlow<ClusterDetailsUiState>(ClusterDetailsUiState.Loading)
    val detailsState: StateFlow<ClusterDetailsUiState> = _detailsState.asStateFlow()

    private val _rebalancingState = MutableStateFlow<RebalancingUiState>(RebalancingUiState.Idle)
    val rebalancingState: StateFlow<RebalancingUiState> = _rebalancingState.asStateFlow()

    private val _showBlockingDialog = MutableStateFlow<String?>(null)
    val showBlockingDialog: StateFlow<String?> = _showBlockingDialog.asStateFlow()

    init {
        loadDetails()
    }

    fun loadDetails() {
        viewModelScope.launch {
            _detailsState.update { ClusterDetailsUiState.Loading }
            when (val result = getClusterDetailsUseCase(clusterId)) {
                is AppResult.Success -> {
                    _detailsState.update { ClusterDetailsUiState.Success(result.data) }
                }
                is AppResult.Error -> {
                    _detailsState.update { ClusterDetailsUiState.Error(result.message) }
                }
                is AppResult.NetworkError -> {
                    _detailsState.update {
                        ClusterDetailsUiState.Error("Network error. Please check your connection.")
                    }
                }
            }
        }
    }

    fun triggerRebalancing() {
        val currentState = _detailsState.value
        if (currentState !is ClusterDetailsUiState.Success) return

        val cluster = currentState.cluster

        val allowedStatuses = setOf(ClusterStatus.READY)
        if (cluster.status !in allowedStatuses) {
            _showBlockingDialog.update { cluster.status.name.lowercase() }
            return
        }

        viewModelScope.launch {
            _rebalancingState.update { RebalancingUiState.CreatingPlan }

            val result = triggerRebalancingUseCase(
                clusterId = clusterId,
                minNodes = 1,
                onPlanCreated = { planId ->
                    _rebalancingState.update { RebalancingUiState.WaitingToExecute(planId) }
                },
            )

            when (result) {
                is AppResult.Success -> {
                    _rebalancingState.update {
                        RebalancingUiState.Success(
                            "Rebalancing executed successfully.\n" +
                                "Plan: ${result.data.rebalancingPlanId}\n" +
                                "Status: ${result.data.status}"
                        )
                    }
                }
                is AppResult.Error -> {
                    _rebalancingState.update { RebalancingUiState.Failure(result.message) }
                }
                is AppResult.NetworkError -> {
                    _rebalancingState.update {
                        RebalancingUiState.Failure("Network error during rebalancing.")
                    }
                }
            }
        }
    }

    fun dismissBlockingDialog() {
        _showBlockingDialog.update { null }
    }

    fun resetRebalancingState() {
        _rebalancingState.update { RebalancingUiState.Idle }
    }
}
