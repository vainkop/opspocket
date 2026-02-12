package com.vainkop.opspocket.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegionDto(
    val name: String? = null,
    val displayName: String? = null,
)
