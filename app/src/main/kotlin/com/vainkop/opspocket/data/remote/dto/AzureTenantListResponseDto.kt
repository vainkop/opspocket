package com.vainkop.opspocket.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AzureTenantListResponseDto(
    val value: List<AzureTenantDto>? = null,
)
