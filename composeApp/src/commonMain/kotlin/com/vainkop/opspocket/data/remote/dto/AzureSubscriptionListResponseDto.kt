package com.vainkop.opspocket.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AzureSubscriptionListResponseDto(
    val value: List<AzureSubscriptionDto>? = null,
)
