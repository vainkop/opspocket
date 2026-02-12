package com.vainkop.opspocket.presentation.apikey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vainkop.opspocket.data.local.SecureApiKeyStorage
import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.usecase.ValidateApiKeyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApiKeyUiState(
    val apiKey: String = "",
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val errorMessage: String? = null,
    val hasExistingKey: Boolean = false
)

@HiltViewModel
class ApiKeyViewModel @Inject constructor(
    private val validateApiKeyUseCase: ValidateApiKeyUseCase,
    private val secureApiKeyStorage: SecureApiKeyStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApiKeyUiState())
    val uiState: StateFlow<ApiKeyUiState> = _uiState.asStateFlow()

    init {
        val hasKey = secureApiKeyStorage.hasApiKey()
        _uiState.update { it.copy(hasExistingKey = hasKey) }
    }

    fun onApiKeyChange(key: String) {
        _uiState.update { it.copy(apiKey = key, errorMessage = null) }
    }

    fun connect() {
        val key = _uiState.value.apiKey.trim()
        if (key.isBlank()) {
            _uiState.update { it.copy(errorMessage = "API key cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isConnecting = true, errorMessage = null) }

            secureApiKeyStorage.saveApiKey(key)

            when (val result = validateApiKeyUseCase()) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isConnecting = false, isConnected = true) }
                }
                is AppResult.Error -> {
                    secureApiKeyStorage.clearApiKey()
                    _uiState.update {
                        it.copy(
                            isConnecting = false,
                            errorMessage = result.message,
                            hasExistingKey = false
                        )
                    }
                }
                is AppResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isConnecting = false,
                            errorMessage = "Network error. Please check your connection and try again."
                        )
                    }
                }
            }
        }
    }

    fun deleteApiKey() {
        secureApiKeyStorage.clearApiKey()
        _uiState.update {
            ApiKeyUiState(hasExistingKey = false)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
