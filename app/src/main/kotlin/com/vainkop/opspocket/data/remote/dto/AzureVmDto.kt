package com.vainkop.opspocket.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AzureVmDto(
    val id: String,
    val name: String? = null,
    val location: String? = null,
    val properties: AzureVmPropertiesDto? = null,
)

@Serializable
data class AzureVmPropertiesDto(
    val hardwareProfile: AzureHardwareProfileDto? = null,
    val provisioningState: String? = null,
    val instanceView: AzureInstanceViewDto? = null,
)

@Serializable
data class AzureHardwareProfileDto(
    val vmSize: String? = null,
)

@Serializable
data class AzureInstanceViewDto(
    val statuses: List<AzureInstanceViewStatusDto>? = null,
)

@Serializable
data class AzureInstanceViewStatusDto(
    val code: String? = null,
    val displayStatus: String? = null,
)
