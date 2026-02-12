package com.vainkop.opspocket.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RebalancingPlanExecuteResponseDto(
    val clusterId: String? = null,
    val rebalancingPlanId: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
