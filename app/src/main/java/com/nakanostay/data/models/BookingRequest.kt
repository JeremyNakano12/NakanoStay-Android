package com.nakanostay.data.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class BookingRequest(
    @SerializedName("guest_name")
    val guestName: String,
    @SerializedName("guest_dni")
    val guestDni: String,
    @SerializedName("guest_email")
    val guestEmail: String,
    @SerializedName("guest_phone")
    val guestPhone: String?,
    @SerializedName("check_in")
    val checkIn: LocalDate,
    @SerializedName("check_out")
    val checkOut: LocalDate,
    val status: String = "PENDING",
    val details: List<BookingDetailRequest>
)

data class BookingDetailRequest(
    @SerializedName("room_id")
    val roomId: Long,
    val guests: Int
)