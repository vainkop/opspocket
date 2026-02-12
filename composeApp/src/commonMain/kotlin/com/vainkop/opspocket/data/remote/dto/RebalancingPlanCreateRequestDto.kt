package com.vainkop.opspocket.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RebalancingPlanCreateRequestDto(
    val minNodes: Int = 1,
)
