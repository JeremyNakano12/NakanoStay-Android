package com.nakanostay.data.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class Booking(
    val id: Long,
    @SerializedName("booking_code")
    val bookingCode: String,
    @SerializedName("guest_name")
    val guestName: String,
    @SerializedName("guest_dni")
    val guestDni: String,
    @SerializedName("guest_email")
    val guestEmail: String,
    @SerializedName("guest_phone")
    val guestPhone: String?,
    @SerializedName("booking_date")
    val bookingDate: LocalDateTime,
    @SerializedName("check_in")
    val checkIn: LocalDate,
    @SerializedName("check_out")
    val checkOut: LocalDate,
    val status: BookingStatus,
    val total: BigDecimal,
    val details: List<BookingDetail>
)