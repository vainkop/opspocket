package com.vainkop.opspocket.presentation.vmdetails

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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vainkop.opspocket.domain.model.AzureVm
import com.vainkop.opspocket.domain.model.VmAction
import com.vainkop.opspocket.domain.model.VmPowerState
import com.vainkop.opspocket.presentation.common.ErrorState
import com.vainkop.opspocket.presentation.common.LoadingIndicator
import com.vainkop.opspocket.presentation.common.VmPowerStateChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: VmDetailsViewModel = hiltViewModel(),
) {
    val detailsState by viewModel.detailsState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (val state = detailsState) {
                            is VmDetailsUiState.Success -> state.vm.name
                            else -> "VM Details"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = detailsState) {
            is VmDetailsUiState.Loading -> {
                LoadingIndicator(
                    message = "Loading VM details...",
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is VmDetailsUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = viewModel::loadDetails,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is VmDetailsUiState.Success -> {
                VmDetailsContent(
                    vm = state.vm,
                    actionState = actionState,
                    onPerformAction = viewModel::performAction,
                    onResetAction = viewModel::resetActionState,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VmDetailsContent(
    vm: AzureVm,
    actionState: VmActionUiState,
    onPerformAction: (VmAction) -> Unit,
    onResetAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingAction by remember { mutableStateOf<VmAction?>(null) }

    pendingAction?.let { action ->
        ConfirmActionDialog(
            action = action,
            vmName = vm.name,
            onConfirm = {
                pendingAction = null
                onPerformAction(action)
            },
            onDismiss = { pendingAction = null },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "VM Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "Name", value = vm.name)
                DetailRow(label = "Resource Group", value = vm.resourceGroup)
                DetailRow(label = "Location", value = vm.location)
                DetailRow(label = "Size", value = vm.vmSize)
                DetailRow(label = "Provisioning", value = vm.provisioningState)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    VmPowerStateChip(powerState = vm.powerState)
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
                    text = "Power Operations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Manage the power state of this virtual machine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))

                when (actionState) {
                    is VmActionUiState.Idle -> {
                        PowerOperationButtons(
                            powerState = vm.powerState,
                            onAction = { pendingAction = it },
                        )
                    }
                    is VmActionUiState.Executing -> {
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
                                text = "${actionState.action.name.lowercase().replaceFirstChar { it.uppercase() }}ing VM...",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    is VmActionUiState.Success -> {
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
                                text = actionState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onResetAction) {
                            Text("Done")
                        }
                    }
                    is VmActionUiState.Failure -> {
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
                                text = actionState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onResetAction) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PowerOperationButtons(
    powerState: VmPowerState,
    onAction: (VmAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { onAction(VmAction.START) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.medium,
            enabled = powerState == VmPowerState.STOPPED || powerState == VmPowerState.DEALLOCATED,
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Start")
        }
        OutlinedButton(
            onClick = { onAction(VmAction.STOP) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.medium,
            enabled = powerState == VmPowerState.RUNNING,
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Stop")
        }
        OutlinedButton(
            onClick = { onAction(VmAction.DEALLOCATE) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.medium,
            enabled = powerState == VmPowerState.RUNNING || powerState == VmPowerState.STOPPED,
        ) {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Deallocate")
        }
        OutlinedButton(
            onClick = { onAction(VmAction.RESTART) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.medium,
            enabled = powerState == VmPowerState.RUNNING,
        ) {
            Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Restart")
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
private fun ConfirmActionDialog(
    action: VmAction,
    vmName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val actionName = action.name.lowercase().replaceFirstChar { it.uppercase() }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = { Text("$actionName VM?") },
        text = {
            Text("Are you sure you want to $actionName \"$vmName\"?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (action != VmAction.START) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
            ) {
                Text(actionName)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
