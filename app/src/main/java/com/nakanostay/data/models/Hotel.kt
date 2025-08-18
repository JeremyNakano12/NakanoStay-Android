package com.nakanostay.data.models

data class Hotel(
    val id: Long,
    val name: String,
    val address: String,
    val city: String?,
    val stars: Int?,
    val email: String
)