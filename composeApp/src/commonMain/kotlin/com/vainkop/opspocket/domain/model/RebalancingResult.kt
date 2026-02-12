package com.vainkop.opspocket.domain.model

data class RebalancingResult(
    val clusterId: String,
    val rebalancingPlanId: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
)
