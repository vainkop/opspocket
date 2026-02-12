package com.vainkop.opspocket.domain.model

data class AzureVm(
    val id: String,
    val name: String,
    val resourceGroup: String,
    val location: String,
    val vmSize: String,
    val provisioningState: String,
    val powerState: VmPowerState,
)
