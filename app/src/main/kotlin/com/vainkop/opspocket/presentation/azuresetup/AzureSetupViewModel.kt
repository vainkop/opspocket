package com.vainkop.opspocket.presentation.azuresetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vainkop.opspocket.data.local.AzureAuthManager
import com.vainkop.opspocket.data.local.AzurePreferences
import com.vainkop.opspocket.domain.model.AppResult
import com.vainkop.opspocket.domain.model.AzureSubscription
import com.vainkop.opspocket.domain.model.AzureTenant
import com.vainkop.opspocket.domain.usecase.GetSubscriptionsUseCase
import com.vainkop.opspocket.domain.usecase.GetTenantsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AzureSetupUiState {
    data object LoadingTenants : AzureSetupUiState()
    data class SelectTenant(val tenants: List<AzureTenant>) : AzureSetupUiState()
    data class LoadingSubscriptions(val tenant: AzureTenant) : AzureSetupUiState()
    data class SelectSubscription(
        val tenant: AzureTenant,
        val subscriptions: List<AzureSubscription>,
    ) : AzureSetupUiState()
    data object Ready : AzureSetupUiState()
    data class Error(val message: String, val step: String) : AzureSetupUiState()
}

@HiltViewModel
class AzureSetupViewModel @Inject constructor(
    private val getTenantsUseCase: GetTenantsUseCase,
    private val getSubscriptionsUseCase: GetSubscriptionsUseCase,
    private val azurePreferences: AzurePreferences,
    private val authManager: AzureAuthManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AzureSetupUiState>(AzureSetupUiState.LoadingTenants)
    val uiState: StateFlow<AzureSetupUiState> = _uiState.asStateFlow()

    init {
        checkPersistedSelection()
    }

    private fun checkPersistedSelection() {
        viewModelScope.launch {
            val selection = azurePreferences.getSelection()
            if (selection.isComplete) {
                // Ensure token is valid for the selected tenant
                val refreshed = authManager.ensureValidToken(selection.tenantId)
                if (refreshed) {
                    _uiState.update { AzureSetupUiState.Ready }
                } else {
                    loadTenants()
                }
            } else {
                loadTenants()
            }
        }
    }

    private fun loadTenants() {
        viewModelScope.launch {
            _uiState.update { AzureSetupUiState.LoadingTenants }
            when (val result = getTenantsUseCase()) {
                is AppResult.Success -> {
                    if (result.data.isEmpty()) {
                        _uiState.update {
                            AzureSetupUiState.Error(
                                "No Azure tenants found for this account.",
                                "tenants",
                            )
                        }
                    } else if (result.data.size == 1) {
                        // Auto-select if only one tenant
                        selectTenant(result.data.first())
                    } else {
                        _uiState.update { AzureSetupUiState.SelectTenant(result.data) }
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        AzureSetupUiState.Error(result.message, "tenants")
                    }
                }
                is AppResult.NetworkError -> {
                    _uiState.update {
                        AzureSetupUiState.Error(
                            "Network error. Please check your connection.",
                            "tenants",
                        )
                    }
                }
            }
        }
    }

    fun selectTenant(tenant: AzureTenant) {
        viewModelScope.launch {
            _uiState.update { AzureSetupUiState.LoadingSubscriptions(tenant) }

            // Acquire tenant-scoped token
            val refreshed = authManager.refreshAccessToken(tenant.tenantId)
            if (!refreshed) {
                _uiState.update {
                    AzureSetupUiState.Error(
                        "Failed to authenticate with tenant ${tenant.displayName}.",
                        "subscriptions",
                    )
                }
                return@launch
            }

            azurePreferences.saveTenantSelection(tenant.tenantId, tenant.displayName)

            when (val result = getSubscriptionsUseCase()) {
                is AppResult.Success -> {
                    if (result.data.isEmpty()) {
                        _uiState.update {
                            AzureSetupUiState.Error(
                                "No subscriptions found in tenant ${tenant.displayName}.",
                                "subscriptions",
                            )
                        }
                    } else if (result.data.size == 1) {
                        selectSubscription(result.data.first())
                    } else {
                        _uiState.update {
                            AzureSetupUiState.SelectSubscription(tenant, result.data)
                        }
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        AzureSetupUiState.Error(result.message, "subscriptions")
                    }
                }
                is AppResult.NetworkError -> {
                    _uiState.update {
                        AzureSetupUiState.Error(
                            "Network error. Please check your connection.",
                            "subscriptions",
                        )
                    }
                }
            }
        }
    }

    fun selectSubscription(subscription: AzureSubscription) {
        viewModelScope.launch {
            azurePreferences.saveSubscriptionSelection(
                subscription.subscriptionId,
                subscription.displayName,
            )
            _uiState.update { AzureSetupUiState.Ready }
        }
    }

    fun changeTenant() {
        loadTenants()
    }

    fun forceReselect() {
        // Skip persisted selection check, go straight to tenant selection
        loadTenants()
    }

    fun retry() {
        val currentState = _uiState.value
        if (currentState is AzureSetupUiState.Error) {
            when (currentState.step) {
                "tenants" -> loadTenants()
                "subscriptions" -> {
                    val selection = viewModelScope.launch {
                        val sel = azurePreferences.getSelection()
                        if (sel.tenantId != null) {
                            selectTenant(AzureTenant(sel.tenantId, sel.tenantName.orEmpty()))
                        } else {
                            loadTenants()
                        }
                    }
                }
            }
        }
    }
}
