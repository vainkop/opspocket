package com.vainkop.opspocket.presentation.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vainkop.opspocket.domain.model.AgentStatus
import com.vainkop.opspocket.domain.model.ClusterStatus
import com.vainkop.opspocket.domain.model.VmPowerState

@Composable
fun ClusterStatusChip(status: ClusterStatus, modifier: Modifier = Modifier) {
    val (backgroundColor, contentColor, label) = when (status) {
        ClusterStatus.READY -> Triple(Color(0xFF1B5E20), Color.White, "Ready")
        ClusterStatus.CONNECTING -> Triple(Color(0xFFF9A825), Color.Black, "Connecting")
        ClusterStatus.WARNING -> Triple(Color(0xFFE65100), Color.White, "Warning")
        ClusterStatus.FAILED -> Triple(Color(0xFFB71C1C), Color.White, "Failed")
        ClusterStatus.DELETING -> Triple(Color(0xFF4A148C), Color.White, "Deleting")
        ClusterStatus.DELETED -> Triple(Color(0xFF424242), Color.White, "Deleted")
        ClusterStatus.HIBERNATING -> Triple(Color(0xFF0D47A1), Color.White, "Hibernating")
        ClusterStatus.HIBERNATED -> Triple(Color(0xFF1A237E), Color.White, "Hibernated")
        ClusterStatus.RESUMING -> Triple(Color(0xFF00695C), Color.White, "Resuming")
        ClusterStatus.UNKNOWN -> Triple(Color(0xFF616161), Color.White, "Unknown")
    }
    StatusChipInternal(
        label = label,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        modifier = modifier,
    )
}

@Composable
fun AgentStatusChip(status: AgentStatus, modifier: Modifier = Modifier) {
    val (backgroundColor, contentColor, label) = when (status) {
        AgentStatus.ONLINE -> Triple(Color(0xFF1B5E20), Color.White, "Online")
        AgentStatus.WAITING_CONNECTION -> Triple(Color(0xFFF9A825), Color.Black, "Waiting")
        AgentStatus.NON_RESPONDING -> Triple(Color(0xFFE65100), Color.White, "Not Responding")
        AgentStatus.DISCONNECTED -> Triple(Color(0xFFB71C1C), Color.White, "Disconnected")
        AgentStatus.DISCONNECTING -> Triple(Color(0xFF4A148C), Color.White, "Disconnecting")
        AgentStatus.UNKNOWN -> Triple(Color(0xFF616161), Color.White, "Unknown")
    }
    StatusChipInternal(
        label = label,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        modifier = modifier,
    )
}

@Composable
fun VmPowerStateChip(powerState: VmPowerState, modifier: Modifier = Modifier) {
    val (backgroundColor, contentColor, label) = when (powerState) {
        VmPowerState.RUNNING -> Triple(Color(0xFF1B5E20), Color.White, "Running")
        VmPowerState.STOPPED -> Triple(Color(0xFFB71C1C), Color.White, "Stopped")
        VmPowerState.DEALLOCATED -> Triple(Color(0xFF616161), Color.White, "Deallocated")
        VmPowerState.STARTING -> Triple(Color(0xFF00695C), Color.White, "Starting")
        VmPowerState.STOPPING -> Triple(Color(0xFFE65100), Color.White, "Stopping")
        VmPowerState.DEALLOCATING -> Triple(Color(0xFF4A148C), Color.White, "Deallocating")
        VmPowerState.UNKNOWN -> Triple(Color(0xFF616161), Color.White, "Unknown")
    }
    StatusChipInternal(
        label = label,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        modifier = modifier,
    )
}

@Composable
internal fun StatusChipInternal(
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
