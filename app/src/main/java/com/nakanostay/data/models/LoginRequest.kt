package com.nakanostay.data.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("apikey")
    val apiKey: String
)