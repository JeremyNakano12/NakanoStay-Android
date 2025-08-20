package com.nakanostay.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakanostay.data.api.ApiService
import com.nakanostay.data.models.Hotel
import com.nakanostay.data.models.Room
import com.nakanostay.data.models.RoomRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class AdminRoomsUiState(
    val rooms: List<Room> = emptyList(),
    val filteredRooms: List<Room> = emptyList(),
    val availableHotels: List<Hotel> = emptyList(),
    val availableRoomTypes: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedHotel: Hotel? = null,
    val selectedRoomType: String? = null,
    val showCreateDialog: Boolean = false,
    val editingRoom: Room? = null,
    val isCreatingOrUpdating: Boolean = false
)

class AdminRoomsViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminRoomsUiState())
    val uiState: StateFlow<AdminRoomsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val hotelsResponse = apiService.getAllHotels()
                val roomsResponse = apiService.getAllRooms()

                if (hotelsResponse.isSuccessful && roomsResponse.isSuccessful) {
                    val hotels = hotelsResponse.body() ?: emptyList()
                    val rooms = roomsResponse.body() ?: emptyList()
                    val roomTypes = rooms.mapNotNull { it.roomType }.distinct().sorted()

                    _uiState.value = _uiState.value.copy(
                        rooms = rooms,
                        availableHotels = hotels,
                        availableRoomTypes = roomTypes,
                        isLoading = false,
                        errorMessage = null
                    )

                    applyFilters()
                } else {
                    val error = when {
                        !hotelsResponse.isSuccessful -> "Error al cargar hoteles: ${hotelsResponse.code()}"
                        !roomsResponse.isSuccessful -> "Error al cargar habitaciones: ${roomsResponse.code()}"
                        else -> "Error desconocido"
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error de conexión: ${e.message}"
                )
            }
        }
    }

    fun refreshRooms() {
        loadData()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun updateSelectedHotel(hotel: Hotel?) {
        _uiState.value = _uiState.value.copy(selectedHotel = hotel)
        applyFilters()
    }

    fun updateSelectedRoomType(roomType: String?) {
        _uiState.value = _uiState.value.copy(selectedRoomType = roomType)
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        var filteredRooms = currentState.rooms

        if (currentState.searchQuery.isNotBlank()) {
            filteredRooms = filteredRooms.filter { room ->
                room.roomNumber.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        if (currentState.selectedHotel != null) {
            filteredRooms = filteredRooms.filter { room ->
                room.hotelId == currentState.selectedHotel.id
            }
        }

        if (currentState.selectedRoomType != null) {
            filteredRooms = filteredRooms.filter { room ->
                room.roomType == currentState.selectedRoomType
            }
        }

        _uiState.value = currentState.copy(filteredRooms = filteredRooms)
    }

    fun setShowCreateDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreateDialog = show)
    }

    fun setEditingRoom(room: Room?) {
        _uiState.value = _uiState.value.copy(editingRoom = room)
    }

    fun createRoom(
        hotelId: Long,
        roomNumber: String,
        roomType: String?,
        pricePerNight: BigDecimal,
        isAvailable: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingOrUpdating = true)

            try {
                val roomRequest = RoomRequest(
                    hotelId = hotelId,
                    roomNumber = roomNumber,
                    roomType = roomType,
                    pricePerNight = pricePerNight,
                    isAvailable = isAvailable
                )

                val response = apiService.createRoom(roomRequest)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrUpdating = false,
                        showCreateDialog = false
                    )
                    loadData()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrUpdating = false,
                        errorMessage = getErrorMessage(response.code(), response.message())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingOrUpdating = false,
                    errorMessage = "Error al crear habitación: ${e.message}"
                )
            }
        }
    }

    fun updateRoom(
        roomId: Long,
        hotelId: Long,
        roomNumber: String,
        roomType: String?,
        pricePerNight: BigDecimal,
        isAvailable: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingOrUpdating = true)

            try {
                val roomRequest = RoomRequest(
                    hotelId = hotelId,
                    roomNumber = roomNumber,
                    roomType = roomType,
                    pricePerNight = pricePerNight,
                    isAvailable = isAvailable
                )

                val response = apiService.updateRoom(roomId, roomRequest)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrUpdating = false,
                        editingRoom = null
                    )
                    loadData()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrUpdating = false,
                        errorMessage = getErrorMessage(response.code(), response.message())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingOrUpdating = false,
                    errorMessage = "Error al actualizar habitación: ${e.message}"
                )
            }
        }
    }

    fun deleteRoom(roomId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteRoom(roomId)
                if (response.isSuccessful) {
                    loadData()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = getErrorMessage(response.code(), response.message())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al eliminar habitación: ${e.message}"
                )
            }
        }
    }

    fun toggleRoomAvailability(room: Room) {
        viewModelScope.launch {
            try {
                val response = if (room.isAvailable) {
                    apiService.makeRoomUnavailable(room.id!!)
                } else {
                    apiService.makeRoomAvailable(room.id!!)
                }

                if (response.isSuccessful) {
                    loadData()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = getErrorMessage(response.code(), response.message())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cambiar disponibilidad: ${e.message}"
                )
            }
        }
    }

    private fun getErrorMessage(code: Int, message: String?): String {
        return when (code) {
            400 -> "Datos inválidos. Verifique la información ingresada"
            401 -> "No autorizado. Inicie sesión nuevamente"
            403 -> "No tiene permisos para realizar esta acción"
            404 -> "Habitación o hotel no encontrado"
            409 -> when {
                message?.contains("habitación") == true -> "Ya existe una habitación con este número en el hotel"
                message?.contains("disponible") == true -> "La habitación ya tiene el estado solicitado"
                else -> "Conflicto al procesar la solicitud"
            }
            422 -> "Error de validación en los datos ingresados"
            500 -> "Error interno del servidor. Intente más tarde"
            else -> "Error: $code ${message ?: "Error desconocido"}"
        }
    }

    fun getHotelById(hotelId: Long): Hotel? {
        return _uiState.value.availableHotels.find { it.id == hotelId }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}