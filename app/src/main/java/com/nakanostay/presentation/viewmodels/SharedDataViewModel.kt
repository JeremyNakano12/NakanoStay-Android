package com.nakanostay.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.nakanostay.data.models.RoomWithHotel
import com.nakanostay.data.models.Hotel
import com.nakanostay.data.models.Booking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedDataViewModel : ViewModel() {

    private val _selectedRoomWithHotel = MutableStateFlow<RoomWithHotel?>(null)
    val selectedRoomWithHotel: StateFlow<RoomWithHotel?> = _selectedRoomWithHotel.asStateFlow()

    private val _selectedHotel = MutableStateFlow<Hotel?>(null)
    val selectedHotel: StateFlow<Hotel?> = _selectedHotel.asStateFlow()

    private val _selectedBooking = MutableStateFlow<Booking?>(null)
    val selectedBooking: StateFlow<Booking?> = _selectedBooking.asStateFlow()

    fun setSelectedRoom(roomWithHotel: RoomWithHotel) {
        _selectedRoomWithHotel.value = roomWithHotel
    }

    fun clearSelectedRoom() {
        _selectedRoomWithHotel.value = null
    }

    fun setSelectedHotel(hotel: Hotel) {
        _selectedHotel.value = hotel
    }

    fun clearSelectedHotel() {
        _selectedHotel.value = null
    }

    fun setSelectedBooking(booking: Booking) {
        _selectedBooking.value = booking
    }

    fun clearSelectedBooking() {
        _selectedBooking.value = null
    }
}