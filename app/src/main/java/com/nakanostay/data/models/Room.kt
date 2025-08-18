package com.nakanostay.data.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Room(
    val id: Long,
    @SerializedName("hotel_id")
    val hotelId: Long,
    @SerializedName("room_number")
    val roomNumber: String,
    @SerializedName("room_type")
    val roomType: String?,
    @SerializedName("price_per_night")
    val pricePerNight: BigDecimal,
    @SerializedName("is_available")
    val isAvailable: Boolean
)