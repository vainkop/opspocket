package com.vainkop.opspocket.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AzureTenantDto(
    val tenantId: String,
    val displayName: String? = null,
)
