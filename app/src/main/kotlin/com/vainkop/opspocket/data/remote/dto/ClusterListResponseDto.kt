package com.vainkop.opspocket.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClusterListResponseDto(
    val items: List<ClusterDto>? = null,
)
