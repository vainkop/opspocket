package com.vainkop.opspocket.domain.model

data class DeviceCodeInfo(
    val userCode: String,
    val verificationUri: String,
    val deviceCode: String,
    val expiresInSeconds: Int,
    val pollingIntervalSeconds: Int,
    val message: String,
)
