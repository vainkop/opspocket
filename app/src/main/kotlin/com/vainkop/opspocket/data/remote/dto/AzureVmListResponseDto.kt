package com.vainkop.opspocket.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AzureVmListResponseDto(
    val value: List<AzureVmDto>? = null,
)
