package com.nakanostay.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakanostay.data.api.ApiService
import com.nakanostay.data.models.Booking
import com.nakanostay.data.models.BookingStatus
import com.nakanostay.data.models.Hotel
import com.nakanostay.data.models.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminBookingsUiState(
    val bookings: List<Booking> = emptyList(),
    val filteredBookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedStatus: BookingStatus? = null,
    val availableStatuses: List<BookingStatus> = BookingStatus.values().toList(),
    val isConfirming: Boolean = false,
    val isCancelling: Boolean = false,
    val isCompleting: Boolean = false,
    val selectedBooking: Booking? = null
)

class AdminBookingsViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminBookingsUiState())
    val uiState: StateFlow<AdminBookingsUiState> = _uiState.asStateFlow()

    private var roomsById: Map<Long, Room> = emptyMap()
    private var hotelsById: Map<Long, Hotel> = emptyMap()

    init {
        loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val bookingsResponse = apiService.getAllBookings()
                val roomsResponse = apiService.getAllRooms()
                val hotelsResponse = apiService.getAllHotels()

                if (bookingsResponse.isSuccessful && roomsResponse.isSuccessful && hotelsResponse.isSuccessful) {
                    val bookings = bookingsResponse.body() ?: emptyList()
                    val rooms = roomsResponse.body() ?: emptyList()
                    val hotels = hotelsResponse.body() ?: emptyList()

                    roomsById = rooms.associateBy { it.id }
                    hotelsById = hotels.associateBy { it.id }

                    val enrichedBookings = bookings.map { booking ->
                        enrichBookingWithRoomsAndHotels(booking)
                    }

                    _uiState.value = _uiState.value.copy(
                        bookings = enrichedBookings,
                        isLoading = false
                    )

                    applyFilters()
                } else {
                    val error = when {
                        !bookingsResponse.isSuccessful -> "Error al cargar reservas: ${bookingsResponse.code()}"
                        !roomsResponse.isSuccessful -> "Error al cargar habitaciones: ${roomsResponse.code()}"
                        !hotelsResponse.isSuccessful -> "Error al cargar hoteles: ${hotelsResponse.code()}"
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

    fun refreshBookings() {
        loadBookings()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun updateSelectedStatus(status: BookingStatus?) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        var filtered = currentState.bookings

        if (currentState.searchQuery.isNotBlank()) {
            val query = currentState.searchQuery.trim().lowercase()
            filtered = filtered.filter { booking ->
                booking.bookingCode.lowercase().contains(query) ||
                        booking.guestDni.contains(query) ||
                        booking.guestName.lowercase().contains(query)
            }
        }

        currentState.selectedStatus?.let { status ->
            filtered = filtered.filter { it.status == status }
        }

        _uiState.value = currentState.copy(filteredBookings = filtered)
    }

    fun confirmBooking(bookingCode: String, guestDni: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConfirming = true)

            try {
                val response = apiService.confirmBooking(bookingCode, guestDni)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isConfirming = false)
                    refreshSelectedBooking()
                    loadBookings()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isConfirming = false,
                        errorMessage = getErrorMessage(response.code(), response.message())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isConfirming = false,
                    errorMessage = "Error al confirmar reserva: ${e.message}"
                )
            }
        }
    }

    fun cancelBooking(bookingCode: String, guestDni: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCancelling = true)

            try {
                val response = apiService.cancelBooking(bookingCode, guestDni)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isCancelling = false)
                    refreshSelectedBooking()
                    loadBookings()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCancelling = false,
                        errorMessage = getErrorMessage(response.code(), response.message())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCancelling = false,
                    errorMessage = "Error al cancelar reserva: ${e.message}"
                )
            }
        }
    }

    fun completeBooking(bookingCode: String, guestDni: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCompleting = true)

            try {
                val response = apiService.completeBooking(bookingCode, guestDni)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isCompleting = false)
                    refreshSelectedBooking()
                    loadBookings()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCompleting = false,
                        errorMessage = getErrorMessage(response.code(), response.message())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCompleting = false,
                    errorMessage = "Error al completar reserva: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun getErrorMessage(code: Int, message: String?): String {
        return when (code) {
            400 -> "Datos inválidos. Verifique la información"
            401 -> "No autorizado. Inicie sesión nuevamente"
            403 -> "No tiene permisos para realizar esta acción"
            404 -> "Reserva no encontrada"
            409 -> when {
                message?.contains("confirmada") == true -> "La reserva ya está confirmada"
                message?.contains("cancelada") == true -> "No se puede modificar una reserva cancelada"
                message?.contains("completada") == true -> "La reserva ya está completada"
                else -> "Conflicto al procesar la solicitud"
            }
            422 -> "Error de validación en los datos"
            500 -> "Error interno del servidor. Intente más tarde"
            else -> "Error: $code ${message ?: "Error desconocido"}"
        }
    }

    fun getBookingStatusColor(status: BookingStatus): androidx.compose.ui.graphics.Color {
        return when (status) {
            BookingStatus.PENDING -> com.nakanostay.ui.theme.WarningOrange
            BookingStatus.CONFIRMED -> com.nakanostay.ui.theme.SuccessGreen
            BookingStatus.CANCELLED -> com.nakanostay.ui.theme.ErrorRed
            BookingStatus.COMPLETED -> com.nakanostay.ui.theme.InfoBlue
        }
    }

    fun getBookingStatusText(status: BookingStatus): String {
        return when (status) {
            BookingStatus.PENDING -> "Pendiente"
            BookingStatus.CONFIRMED -> "Confirmada"
            BookingStatus.CANCELLED -> "Cancelada"
            BookingStatus.COMPLETED -> "Completada"
        }
    }

    private fun enrichBookingWithRoomsAndHotels(booking: Booking): Booking {
        val enrichedDetails = booking.details.map { detail ->
            val room = roomsById[detail.roomId]
            detail.copy(room = room)
        }
        return booking.copy(details = enrichedDetails)
    }

    fun getRoomById(roomId: Long): Room? {
        return roomsById[roomId]
    }

    fun getHotelById(hotelId: Long): Hotel? {
        return hotelsById[hotelId]
    }

    fun getHotelForBooking(booking: Booking): Hotel? {
        val firstRoom = booking.details.firstOrNull()?.room
        return firstRoom?.let { room ->
            hotelsById[room.hotelId]
        }
    }

    fun setSelectedBooking(booking: Booking) {
        _uiState.value = _uiState.value.copy(selectedBooking = booking)
    }

    fun clearSelectedBooking() {
        _uiState.value = _uiState.value.copy(selectedBooking = null)
    }

    private suspend fun refreshSelectedBooking() {
        val currentBooking = _uiState.value.selectedBooking
        if (currentBooking != null) {
            try {
                val response = apiService.getBookingByCode(currentBooking.bookingCode, currentBooking.guestDni)
                if (response.isSuccessful) {
                    val updatedBooking = response.body()?.let { enrichBookingWithRoomsAndHotels(it) }
                    if (updatedBooking != null) {
                        _uiState.value = _uiState.value.copy(selectedBooking = updatedBooking)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
}