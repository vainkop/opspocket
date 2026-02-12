package com.vainkop.opspocket.data.remote

import com.vainkop.opspocket.data.remote.dto.DeviceCodeResponseDto
import com.vainkop.opspocket.data.remote.dto.TokenResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.parameters

class AzureAuthApiClient(private val httpClient: HttpClient) {

    suspend fun requestDeviceCode(
        tenant: String = "common",
        clientId: String,
        scope: String,
    ): DeviceCodeResponseDto {
        return httpClient.submitForm(
            url = "$tenant/oauth2/v2.0/devicecode",
            formParameters = parameters {
                append("client_id", clientId)
                append("scope", scope)
            }
        ).body()
    }

    suspend fun pollToken(
        tenant: String = "common",
        clientId: String,
        deviceCode: String,
    ): TokenResponseDto {
        return httpClient.submitForm(
            url = "$tenant/oauth2/v2.0/token",
            formParameters = parameters {
                append("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                append("client_id", clientId)
                append("device_code", deviceCode)
            }
        ).body()
    }

    suspend fun refreshToken(
        tenant: String = "common",
        clientId: String,
        refreshToken: String,
        scope: String,
    ): TokenResponseDto {
        return httpClient.submitForm(
            url = "$tenant/oauth2/v2.0/token",
            formParameters = parameters {
                append("grant_type", "refresh_token")
                append("client_id", clientId)
                append("refresh_token", refreshToken)
                append("scope", scope)
            }
        ).body()
    }
}
