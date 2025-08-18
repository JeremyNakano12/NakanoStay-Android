package com.nakanostay.data.models

data class RoomFilters(
    val stars: Int? = null,
    val city: String? = null,
    val hotelId: Long? = null,
    val roomType: String? = null,
    val onlyAvailable: Boolean = false
)
