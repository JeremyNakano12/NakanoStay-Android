package com.nakanostay.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakanostay.data.models.Hotel
import com.nakanostay.data.models.RoomFilters
import com.nakanostay.data.models.RoomWithHotel
import com.nakanostay.data.models.UiState
import com.nakanostay.data.repository.RoomWithHotelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RoomsViewModel(
    private val roomWithHotelRepository: RoomWithHotelRepository
) : ViewModel() {

    // State for all rooms with hotels
    private val _roomsState = MutableStateFlow(UiState<List<RoomWithHotel>>())
    val roomsState: StateFlow<UiState<List<RoomWithHotel>>> = _roomsState.asStateFlow()

    // State for filtered rooms
    private val _filteredRoomsState = MutableStateFlow<List<RoomWithHotel>>(emptyList())
    val filteredRoomsState: StateFlow<List<RoomWithHotel>> = _filteredRoomsState.asStateFlow()

    // State for filters
    private val _filtersState = MutableStateFlow(RoomFilters())
    val filtersState: StateFlow<RoomFilters> = _filtersState.asStateFlow()

    // Available filter options
    private val _availableCities = MutableStateFlow<List<String>>(emptyList())
    val availableCities: StateFlow<List<String>> = _availableCities.asStateFlow()

    private val _availableHotels = MutableStateFlow<List<Hotel>>(emptyList())
    val availableHotels: StateFlow<List<Hotel>> = _availableHotels.asStateFlow()

    private val _availableRoomTypes = MutableStateFlow<List<String>>(emptyList())
    val availableRoomTypes: StateFlow<List<String>> = _availableRoomTypes.asStateFlow()

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadRooms()
    }

    fun loadRooms() {
        viewModelScope.launch {
            _roomsState.value = UiState(isLoading = true)

            roomWithHotelRepository.getAllRoomsWithHotels().collect { result ->
                if (result.isSuccess) {
                    val rooms = result.getOrNull() ?: emptyList()
                    _roomsState.value = UiState(data = rooms)

                    updateFilterOptions(rooms)

                    applyFilters()
                } else {
                    _roomsState.value = UiState(
                        error = result.exceptionOrNull()?.message ?: "Error loading rooms"
                    )
                }
            }
        }
    }

    private fun updateFilterOptions(rooms: List<RoomWithHotel>) {
        val cities = rooms.map { it.hotel.city }.filterNotNull().distinct().sorted()
        _availableCities.value = cities

        val hotels = rooms.map { it.hotel }.distinctBy { it.id }.sortedBy { it.name }
        _availableHotels.value = hotels

        val roomTypes = rooms.map { it.room.roomType }.filterNotNull().distinct().sorted()
        _availableRoomTypes.value = roomTypes
    }

    fun updateFilters(filters: RoomFilters) {
        _filtersState.value = filters
        applyFilters()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun clearFilters() {
        _filtersState.value = RoomFilters()
        _searchQuery.value = ""
        applyFilters()
    }

    private fun applyFilters() {
        val rooms = _roomsState.value.data ?: emptyList()
        val filters = _filtersState.value
        val query = _searchQuery.value

        val filteredRooms = rooms.filter { roomWithHotel ->
            val room = roomWithHotel.room
            val hotel = roomWithHotel.hotel

            val starsMatch = filters.stars?.let { hotel.stars == it } ?: true
            val cityMatch = filters.city?.let { hotel.city == it } ?: true
            val hotelMatch = filters.hotelId?.let { hotel.id == it } ?: true
            val roomTypeMatch = filters.roomType?.let { room.roomType == it } ?: true
            val availabilityMatch = if (filters.onlyAvailable) room.isAvailable else true

            val searchMatch = if (query.isBlank()) {
                true
            } else {
                hotel.name.contains(query, ignoreCase = true) ||
                        room.roomNumber.contains(query, ignoreCase = true) ||
                        (room.roomType?.contains(query, ignoreCase = true) ?: false)
            }

            starsMatch && cityMatch && hotelMatch && roomTypeMatch && availabilityMatch && searchMatch
        }

        _filteredRoomsState.value = filteredRooms
    }

    fun refreshRooms() {
        loadRooms()
    }

    fun getFilteredRoomsCount(): Int = _filteredRoomsState.value.size
    fun getTotalRoomsCount(): Int = _roomsState.value.data?.size ?: 0
}