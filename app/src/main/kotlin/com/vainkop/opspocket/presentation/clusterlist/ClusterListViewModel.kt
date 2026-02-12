package com.vainkop.opspocket.presentation.clusterlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.Cluster
import com.vainkop.opspocket.domain.usecase.GetClustersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ClusterListUiState {
    data object Loading : ClusterListUiState()
    data class Success(val clusters: List<Cluster>) : ClusterListUiState()
    data class Error(val message: String) : ClusterListUiState()
    data object Empty : ClusterListUiState()
}

@HiltViewModel
class ClusterListViewModel @Inject constructor(
    private val getClustersUseCase: GetClustersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClusterListUiState>(ClusterListUiState.Loading)
    val uiState: StateFlow<ClusterListUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadClusters()
    }

    fun loadClusters() {
        viewModelScope.launch {
            if (_uiState.value !is ClusterListUiState.Loading) {
                _uiState.update { ClusterListUiState.Loading }
            }
            fetchClusters()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.update { true }
            fetchClusters()
            _isRefreshing.update { false }
        }
    }

    private suspend fun fetchClusters() {
        when (val result = getClustersUseCase()) {
            is AppResult.Success -> {
                if (result.data.isEmpty()) {
                    _uiState.update { ClusterListUiState.Empty }
                } else {
                    _uiState.update { ClusterListUiState.Success(result.data) }
                }
            }
            is AppResult.Error -> {
                _uiState.update { ClusterListUiState.Error(result.message) }
            }
            is AppResult.NetworkError -> {
                _uiState.update {
                    ClusterListUiState.Error("Network error. Please check your connection.")
                }
            }
        }
    }
}
