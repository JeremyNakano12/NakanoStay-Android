package com.nakanostay.data.models

import com.google.gson.annotations.SerializedName

data class RoomAvailability(
    @SerializedName("room_id")
    val roomId: Long,
    @SerializedName("available_dates")
    val availableDates: List<String>,
    @SerializedName("occupied_ranges")
    val occupiedRanges: List<OccupiedRange>
)

data class OccupiedRange(
    val start: String,
    val end: String
)