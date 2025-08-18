package com.nakanostay.data.models

data class HotelRequest(
    val name: String,
    val address: String,
    val city: String?,
    val stars: Int?,
    val email: String
)