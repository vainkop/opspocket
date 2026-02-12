package com.vainkop.opspocket.data.local

import com.vainkop.opspocket.data.remote.AzureAuthApi
import com.vainkop.opspocket.domain.model.DeviceCodeInfo
import kotlinx.coroutines.delay
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AzureAuthManager @Inject constructor(
    private val authApi: AzureAuthApi,
    private val tokenStorage: AzureTokenStorage,
) {

    val isSignedIn: Boolean
        get() = tokenStorage.hasTokens()

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
        val maxAttempts = 180 // 15 minutes max at 5s interval
        repeat(maxAttempts) {
            delay(intervalMs)
            try {
                val tokenResponse = authApi.pollToken(
                    clientId = CLIENT_ID,
                    deviceCode = deviceCode,
                )
                tokenStorage.saveTokens(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken,
                    expiresInSeconds = tokenResponse.expiresIn,
                )
                return true
            } catch (e: HttpException) {
                // 400 with "authorization_pending" means user hasn't authenticated yet
                // Any other error means we should stop
                if (e.code() != 400) throw e
                val errorBody = e.response()?.errorBody()?.string().orEmpty()
                if ("authorization_pending" !in errorBody && "slow_down" !in errorBody) {
                    throw e
                }
            }
        }
        return false
    }

    suspend fun refreshAccessToken(tenantId: String? = null): Boolean {
        val refreshToken = tokenStorage.getRefreshToken() ?: return false
        return try {
            val response = authApi.refreshToken(
                tenant = tenantId ?: "common",
                clientId = CLIENT_ID,
                refreshToken = refreshToken,
                scope = SCOPE,
            )
            tokenStorage.saveTokens(
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
        if (!tokenStorage.isTokenExpired()) return true
        return refreshAccessToken(tenantId)
    }

    fun getAccessToken(): String? = tokenStorage.getAccessToken()

    fun signOut() {
        tokenStorage.clearTokens()
    }

    companion object {
        const val CLIENT_ID = "1950a258-227b-4e31-a9cf-717495945fc2" // Azure PowerShell public client
        const val SCOPE = "https://management.azure.com/.default offline_access"
    }
}
