package com.vainkop.opspocket.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AzureSubscriptionDto(
    val subscriptionId: String,
    val displayName: String? = null,
    val state: String? = null,
)
