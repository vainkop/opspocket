package com.vainkop.opspocket.data.local

import com.vainkop.opspocket.data.remote.AzureAuthApiClient
import com.vainkop.opspocket.domain.model.DeviceCodeInfo
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.delay

class AzureAuthManager(
    private val authApi: AzureAuthApiClient,
    private val storage: SecureStorage,
) {

    val isSignedIn: Boolean
        get() = storage.getString(KEY_REFRESH_TOKEN) != null

    suspend fun requestDeviceCode(): DeviceCodeInfo {
        val response = authApi.requestDeviceCode(
            clientId = CLIENT_ID,
            scope = SCOPE,
        )
        return DeviceCodeInfo(
            userCode = response.userCode,
            verificationUri = response.verificationUri,
            deviceCode = response.deviceCode,
            expiresInSeconds = response.expiresIn,
            pollingIntervalSeconds = response.interval,
            message = response.message,
        )
    }

    suspend fun pollForToken(deviceCode: String, intervalSeconds: Int): Boolean {
        val intervalMs = intervalSeconds * 1000L
        val maxAttempts = 180
        repeat(maxAttempts) {
            delay(intervalMs)
            try {
                val tokenResponse = authApi.pollToken(
                    clientId = CLIENT_ID,
                    deviceCode = deviceCode,
                )
                saveTokens(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken,
                    expiresInSeconds = tokenResponse.expiresIn,
                )
                return true
            } catch (e: ClientRequestException) {
                if (e.response.status.value != 400) throw e
                val errorBody = e.response.status.description
                if ("authorization_pending" !in errorBody && "slow_down" !in errorBody) {
                    throw e
                }
            } catch (_: Exception) {
                // Transient network error - keep polling
            }
        }
        return false
    }

    suspend fun refreshAccessToken(tenantId: String? = null): Boolean {
        val refreshToken = storage.getString(KEY_REFRESH_TOKEN) ?: return false
        return try {
            val response = authApi.refreshToken(
                tenant = tenantId ?: "common",
                clientId = CLIENT_ID,
                refreshToken = refreshToken,
                scope = SCOPE,
            )
            saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken ?: refreshToken,
                expiresInSeconds = response.expiresIn,
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun ensureValidToken(tenantId: String? = null): Boolean {
        if (!isTokenExpired()) return true
        return refreshAccessToken(tenantId)
    }

    fun getAccessToken(): String? = storage.getString(KEY_ACCESS_TOKEN)

    fun signOut() {
        storage.remove(KEY_ACCESS_TOKEN)
        storage.remove(KEY_REFRESH_TOKEN)
        storage.remove(KEY_EXPIRES_AT)
    }

    private fun saveTokens(accessToken: String, refreshToken: String?, expiresInSeconds: Int) {
        storage.putString(KEY_ACCESS_TOKEN, accessToken)
        refreshToken?.let { storage.putString(KEY_REFRESH_TOKEN, it) }
        storage.putLong(KEY_EXPIRES_AT, currentTimeMillis() + expiresInSeconds * 1000L)
    }

    private fun isTokenExpired(): Boolean {
        val expiresAt = storage.getLong(KEY_EXPIRES_AT)
        return currentTimeMillis() >= expiresAt - 60_000
    }

    companion object {
        const val CLIENT_ID = "1950a258-227b-4e31-a9cf-717495945fc2"
        const val SCOPE = "https://management.azure.com/.default offline_access"
        private const val KEY_ACCESS_TOKEN = "azure_access_token"
        private const val KEY_REFRESH_TOKEN = "azure_refresh_token"
        private const val KEY_EXPIRES_AT = "azure_expires_at"
    }
}

internal expect fun currentTimeMillis(): Long
