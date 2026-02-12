package com.vainkop.opspocket.presentation.azuresetup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vainkop.opspocket.domain.model.AzureSubscription
import com.vainkop.opspocket.domain.model.AzureTenant
import com.vainkop.opspocket.presentation.common.ErrorState
import com.vainkop.opspocket.presentation.common.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzureSetupScreen(
    onSetupComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    forceSetup: Boolean = false,
    viewModel: AzureSetupViewModel = koinViewModel { parametersOf(forceSetup) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is AzureSetupUiState.Ready) {
            onSetupComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Azure Setup") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is AzureSetupUiState.LoadingTenants -> {
                LoadingIndicator(
                    message = "Loading tenants...",
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is AzureSetupUiState.SelectTenant -> {
                TenantList(
                    tenants = state.tenants,
                    onSelectTenant = viewModel::selectTenant,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is AzureSetupUiState.LoadingSubscriptions -> {
                LoadingIndicator(
                    message = "Loading subscriptions for ${state.tenant.displayName}...",
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is AzureSetupUiState.SelectSubscription -> {
                SubscriptionList(
                    tenant = state.tenant,
                    subscriptions = state.subscriptions,
                    onSelectSubscription = viewModel::selectSubscription,
                    onChangeTenant = viewModel::changeTenant,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is AzureSetupUiState.Ready -> {
                LoadingIndicator(
                    message = "Setup complete...",
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is AzureSetupUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = viewModel::retry,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun TenantList(
    tenants: List<AzureTenant>,
    onSelectTenant: (AzureTenant) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Select a Tenant",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(tenants, key = { it.tenantId }) { tenant ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTenant(tenant) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = tenant.displayName.ifBlank { "Unnamed Tenant" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tenant.tenantId,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionList(
    tenant: AzureTenant,
    subscriptions: List<AzureSubscription>,
    onSelectSubscription: (AzureSubscription) -> Unit,
    onChangeTenant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Select a Subscription",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Tenant: ${tenant.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onChangeTenant) {
                Text("Change")
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(subscriptions, key = { it.subscriptionId }) { subscription ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSubscription(subscription) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = subscription.displayName.ifBlank { "Unnamed Subscription" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subscription.subscriptionId,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
