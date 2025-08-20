package com.nakanostay.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.nakanostay.data.models.RoomWithHotel
import com.nakanostay.data.models.Hotel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedDataViewModel : ViewModel() {

    private val _selectedRoomWithHotel = MutableStateFlow<RoomWithHotel?>(null)
    val selectedRoomWithHotel: StateFlow<RoomWithHotel?> = _selectedRoomWithHotel.asStateFlow()

    private val _selectedHotel = MutableStateFlow<Hotel?>(null)
    val selectedHotel: StateFlow<Hotel?> = _selectedHotel.asStateFlow()

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
}