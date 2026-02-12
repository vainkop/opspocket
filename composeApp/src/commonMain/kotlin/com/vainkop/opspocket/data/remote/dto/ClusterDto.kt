package com.vainkop.opspocket.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClusterDto(
    val id: String,
    val name: String? = null,
    val region: RegionDto? = null,
    val status: String? = null,
    val agentStatus: String? = null,
    val providerType: String? = null,
    val createdAt: String? = null,
)
