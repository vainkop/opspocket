package com.vainkop.opspocket.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RebalancingPlanCreateResponseDto(
    val rebalancingPlanId: String,
)
