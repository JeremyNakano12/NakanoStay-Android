package com.nakanostay.data.models

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expiresIn: Int,
    @SerializedName("refresh_token")
    val refreshToken: String,
    val user: SupabaseUser
)

data class SupabaseUser(
    val id: String,
    val email: String,
    val role: String
)