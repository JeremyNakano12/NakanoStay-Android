package com.nakanostay.data.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class BookingDetail(
    @SerializedName("room_id")
    val roomId: Long,
    val guests: Int,
    @SerializedName("price_at_booking")
    val priceAtBooking: BigDecimal
)
