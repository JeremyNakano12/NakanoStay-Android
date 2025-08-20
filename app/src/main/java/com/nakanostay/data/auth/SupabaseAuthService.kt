package com.nakanostay.data.auth

import com.nakanostay.data.models.AuthResponse
import com.nakanostay.data.models.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface SupabaseAuthService {
    @Headers("Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    suspend fun login(
        @Header("apikey") apiKey: String,
        @Body request: LoginRequest
    ): Response<AuthResponse>
}