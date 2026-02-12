package com.vainkop.opspocket.data.remote

import com.vainkop.opspocket.data.remote.dto.DeviceCodeResponseDto
import com.vainkop.opspocket.data.remote.dto.TokenResponseDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface AzureAuthApi {

    @FormUrlEncoded
    @POST("{tenant}/oauth2/v2.0/devicecode")
    suspend fun requestDeviceCode(
        @Path("tenant") tenant: String = "common",
        @Field("client_id") clientId: String,
        @Field("scope") scope: String,
    ): DeviceCodeResponseDto

    @FormUrlEncoded
    @POST("{tenant}/oauth2/v2.0/token")
    suspend fun pollToken(
        @Path("tenant") tenant: String = "common",
        @Field("grant_type") grantType: String = "urn:ietf:params:oauth:grants:device-code",
        @Field("client_id") clientId: String,
        @Field("device_code") deviceCode: String,
    ): TokenResponseDto

    @FormUrlEncoded
    @POST("{tenant}/oauth2/v2.0/token")
    suspend fun refreshToken(
        @Path("tenant") tenant: String = "common",
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("client_id") clientId: String,
        @Field("refresh_token") refreshToken: String,
        @Field("scope") scope: String,
    ): TokenResponseDto
}
