package com.nakanostay.data.models

import com.google.gson.annotations.SerializedName

data class RoomRequest(
    @SerializedName("hotel_id")
    val hotelId: Long,
    @SerializedName("room_number")
    val roomNumber: String,
    @SerializedName("room_type")
    val roomType: String?,
    @SerializedName("price_per_night")
    val pricePerNight: java.math.BigDecimal,
    @SerializedName("is_available")
    val isAvailable: Boolean
)