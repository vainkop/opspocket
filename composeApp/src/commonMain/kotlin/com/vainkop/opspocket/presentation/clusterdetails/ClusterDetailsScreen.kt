package com.vainkop.opspocket.presentation.clusterdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vainkop.opspocket.domain.model.Cluster
import com.vainkop.opspocket.presentation.common.AgentStatusChip
import com.vainkop.opspocket.presentation.common.ClusterStatusChip
import com.vainkop.opspocket.presentation.common.ErrorState
import com.vainkop.opspocket.presentation.common.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClusterDetailsScreen(
    clusterId: String,
    onNavigateBack: () -> Unit,
    viewModel: ClusterDetailsViewModel = koinViewModel { parametersOf(clusterId) },
) {
    val detailsState by viewModel.detailsState.collectAsStateWithLifecycle()
    val rebalancingState by viewModel.rebalancingState.collectAsStateWithLifecycle()
    val blockingStatus by viewModel.showBlockingDialog.collectAsStateWithLifecycle()

    blockingStatus?.let { status ->
        BlockingStatusDialog(
            clusterStatus = status,
            onDismiss = viewModel::dismissBlockingDialog,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (val state = detailsState) {
                            is ClusterDetailsUiState.Success -> state.cluster.name
                            else -> "Cluster Details"
                        },
                    )
                },
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
        when (val state = detailsState) {
            is ClusterDetailsUiState.Loading -> {
                LoadingIndicator(
                    message = "Loading cluster details...",
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is ClusterDetailsUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = viewModel::loadDetails,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is ClusterDetailsUiState.Success -> {
                ClusterDetailsContent(
                    cluster = state.cluster,
                    rebalancingState = rebalancingState,
                    onTriggerRebalancing = viewModel::triggerRebalancing,
                    onResetRebalancing = viewModel::resetRebalancingState,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClusterDetailsContent(
    cluster: Cluster,
    rebalancingState: RebalancingUiState,
    onTriggerRebalancing: () -> Unit,
    onResetRebalancing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Cluster Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "Name", value = cluster.name)
                DetailRow(
                    label = "Region",
                    value = cluster.regionDisplayName.ifBlank { cluster.regionName },
                )
                DetailRow(label = "Provider", value = cluster.providerType.uppercase())
                DetailRow(label = "Created", value = cluster.createdAt.take(10))
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ClusterStatusChip(status = cluster.status)
                    AgentStatusChip(status = cluster.agentStatus)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Rebalancing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Optimize node configuration by consolidating workloads into fewer, right-sized nodes. Uses minNodes=1.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))

                when (rebalancingState) {
                    is RebalancingUiState.Idle -> {
                        Button(
                            onClick = onTriggerRebalancing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Trigger Rebalancing")
                        }
                    }
                    is RebalancingUiState.CreatingPlan -> {
                        RebalancingProgressRow(message = "Generating rebalancing plan...")
                    }
                    is RebalancingUiState.WaitingToExecute -> {
                        RebalancingProgressRow(
                            message = "Plan created: ${rebalancingState.planId.take(8)}...\nWaiting before execution...",
                        )
                    }
                    is RebalancingUiState.Executing -> {
                        RebalancingProgressRow(message = "Executing rebalancing plan...")
                    }
                    is RebalancingUiState.Success -> {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = rebalancingState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onResetRebalancing) {
                            Text("Done")
                        }
                    }
                    is RebalancingUiState.Failure -> {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = rebalancingState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onResetRebalancing) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun RebalancingProgressRow(message: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun BlockingStatusDialog(clusterStatus: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = { Text("Cannot Rebalance") },
        text = {
            Text(
                "Cluster status is \"$clusterStatus\". " +
                    "Rebalancing requires the cluster to be in \"ready\" state. " +
                    "Please wait until the cluster is ready before triggering rebalancing.",
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
    )
}
