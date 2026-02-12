package com.vainkop.opspocket.data.remote.interceptor

import com.vainkop.opspocket.data.local.SecureApiKeyStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val secureApiKeyStorage: SecureApiKeyStorage,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val apiKey = secureApiKeyStorage.getApiKey()

        val request = if (apiKey != null) {
            originalRequest.newBuilder()
                .header("X-API-Key", apiKey)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
