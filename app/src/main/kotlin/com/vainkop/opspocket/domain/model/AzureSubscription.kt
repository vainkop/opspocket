package com.vainkop.opspocket.domain.model

data class AzureSubscription(
    val subscriptionId: String,
    val displayName: String,
    val state: String,
)
