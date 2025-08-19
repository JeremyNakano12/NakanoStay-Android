package com.nakanostay.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.nakanostay.data.models.RoomWithHotel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedDataViewModel : ViewModel() {

    // Selected room with hotel for details screen
    private val _selectedRoomWithHotel = MutableStateFlow<RoomWithHotel?>(null)
    val selectedRoomWithHotel: StateFlow<RoomWithHotel?> = _selectedRoomWithHotel.asStateFlow()

    fun setSelectedRoom(roomWithHotel: RoomWithHotel) {
        _selectedRoomWithHotel.value = roomWithHotel
    }

    fun clearSelectedRoom() {
        _selectedRoomWithHotel.value = null
    }
}