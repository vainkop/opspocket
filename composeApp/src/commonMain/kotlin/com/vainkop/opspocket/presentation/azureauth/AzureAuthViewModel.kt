package com.vainkop.opspocket.presentation.azureauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vainkop.opspocket.data.local.AzureAuthManager
import com.vainkop.opspocket.data.local.AzurePreferences
import com.vainkop.opspocket.domain.model.DeviceCodeInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AzureAuthUiState(
    val isChecking: Boolean = true,
    val isSignedIn: Boolean = false,
    val hasExistingSession: Boolean = false,
    val deviceCodeInfo: DeviceCodeInfo? = null,
    val isPolling: Boolean = false,
    val errorMessage: String? = null,
)

class AzureAuthViewModel(
    private val authManager: AzureAuthManager,
    private val azurePreferences: AzurePreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AzureAuthUiState())
    val uiState: StateFlow<AzureAuthUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            if (authManager.isSignedIn) {
                val refreshed = authManager.ensureValidToken()
                if (refreshed) {
                    _uiState.update {
                        it.copy(isChecking = false, hasExistingSession = true)
                    }
                } else {
                    authManager.signOut()
                    _uiState.update { it.copy(isChecking = false) }
                }
            } else {
                _uiState.update { it.copy(isChecking = false) }
            }
        }
    }

    fun continueWithExistingSession() {
        _uiState.update { it.copy(isSignedIn = true) }
    }

    fun startSignIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null, deviceCodeInfo = null) }
            try {
                val deviceCode = authManager.requestDeviceCode()
                _uiState.update { it.copy(deviceCodeInfo = deviceCode) }
                pollForToken(deviceCode)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to start sign-in")
                }
            }
        }
    }

    private fun pollForToken(deviceCode: DeviceCodeInfo) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            _uiState.update { it.copy(isPolling = true) }
            try {
                val success = authManager.pollForToken(
                    deviceCode = deviceCode.deviceCode,
                    intervalSeconds = deviceCode.pollingIntervalSeconds,
                )
                if (success) {
                    _uiState.update {
                        it.copy(isPolling = false, isSignedIn = true, deviceCodeInfo = null)
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isPolling = false,
                            errorMessage = "Sign-in timed out. Please try again.",
                            deviceCodeInfo = null,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPolling = false,
                        errorMessage = e.message ?: "Sign-in failed",
                        deviceCodeInfo = null,
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            pollingJob?.cancel()
            authManager.signOut()
            azurePreferences.clearAll()
            _uiState.update {
                AzureAuthUiState(
                    isChecking = false,
                    hasExistingSession = false,
                )
            }
        }
    }

    fun cancelSignIn() {
        pollingJob?.cancel()
        _uiState.update {
            it.copy(isPolling = false, deviceCodeInfo = null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
