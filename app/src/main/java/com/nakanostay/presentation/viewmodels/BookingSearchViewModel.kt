package com.nakanostay.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakanostay.data.models.BookingStatus
import com.nakanostay.data.models.Booking
import com.nakanostay.data.models.Room
import com.nakanostay.data.models.UiState
import com.nakanostay.data.repository.BookingRepository
import com.nakanostay.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookingSearchViewModel(
    private val bookingRepository: BookingRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    // Search form state
    private val _searchForm = MutableStateFlow(BookingSearchForm())
    val searchForm: StateFlow<BookingSearchForm> = _searchForm.asStateFlow()

    // Search result state
    private val _bookingState = MutableStateFlow(UiState<Booking>())
    val bookingState: StateFlow<UiState<Booking>> = _bookingState.asStateFlow()

    // Cancellation state
    private val _cancellationState = MutableStateFlow(UiState<Booking>())
    val cancellationState: StateFlow<UiState<Booking>> = _cancellationState.asStateFlow()

    // Show cancellation dialog
    private val _showCancellationDialog = MutableStateFlow(false)
    val showCancellationDialog: StateFlow<Boolean> = _showCancellationDialog.asStateFlow()

    private var roomsById: Map<Long, Room> = emptyMap()

    fun updateSearchForm(form: BookingSearchForm) {
        _searchForm.value = form
    }

    fun updateBookingCode(code: String) {
        _searchForm.value = _searchForm.value.copy(bookingCode = code)
    }

    fun updateGuestDni(dni: String) {
        _searchForm.value = _searchForm.value.copy(guestDni = dni)
    }

    fun searchBooking() {
        val form = _searchForm.value

        if (!isSearchFormValid(form)) {
            _bookingState.value = UiState(error = "Por favor completa todos los campos")
            return
        }

        viewModelScope.launch {
            _bookingState.value = UiState(isLoading = true)

            bookingRepository.getBookingByCode(form.bookingCode, form.guestDni).collect { result ->
                if (result.isSuccess) {
                    val booking = result.getOrNull()
                    val enrichedBooking = try {
                        booking?.let { enrichBookingWithRooms(it) }
                    } catch (e: Exception) {
                        booking
                    }

                    _bookingState.value = UiState(data = enrichedBooking)
                } else {
                    _bookingState.value = UiState(
                        error = result.exceptionOrNull()?.message ?: "Reserva no encontrada"
                    )
                }
            }
        }
    }

    fun showCancellationDialog() {
        _showCancellationDialog.value = true
    }

    fun hideCancellationDialog() {
        _showCancellationDialog.value = false
    }

    fun cancelBooking() {
        val form = _searchForm.value
        val booking = _bookingState.value.data

        if (booking == null || !canCancelBooking(booking)) {
            _cancellationState.value = UiState(error = "No se puede cancelar esta reserva")
            return
        }

        viewModelScope.launch {
            _cancellationState.value = UiState(isLoading = true)

            bookingRepository.cancelBooking(form.bookingCode, form.guestDni).collect { result ->
                if (result.isSuccess) {
                    val cancelledBooking = result.getOrNull()
                    _cancellationState.value = UiState(data = cancelledBooking)
                    _bookingState.value = UiState(data = cancelledBooking)
                    _showCancellationDialog.value = false
                } else {
                    _cancellationState.value = UiState(
                        error = result.exceptionOrNull()?.message ?: "Error al cancelar reserva"
                    )
                }
            }
        }
    }

    fun clearSearch() {
        _searchForm.value = BookingSearchForm()
        _bookingState.value = UiState()
        _cancellationState.value = UiState()
        _showCancellationDialog.value = false
    }

    fun clearBookingState() {
        _bookingState.value = UiState()
    }

    fun clearCancellationState() {
        _cancellationState.value = UiState()
    }

    private fun isSearchFormValid(form: BookingSearchForm): Boolean {
        return form.bookingCode.isNotBlank() &&
                form.guestDni.isNotBlank() &&
                form.guestDni.matches("^[0-9]{10}$".toRegex())
    }

    private fun canCancelBooking(booking: Booking): Boolean {
        return booking.status == BookingStatus.PENDING || booking.status == BookingStatus.CONFIRMED
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

    private suspend fun ensureRoomsCache(): Map<Long, Room> {
        if (roomsById.isNotEmpty()) return roomsById

        roomRepository.getAllRooms().collect { roomsResult ->
            if (roomsResult.isSuccess) {
                val list = roomsResult.getOrNull().orEmpty()
                roomsById = list.associateBy { it.id }
            } else {
                roomsById = emptyMap()
            }
        }
        return roomsById
    }


    private suspend fun enrichBookingWithRooms(booking: Booking): Booking {
        val cache = ensureRoomsCache()
        val enrichedDetails = booking.details.map { detail ->
            detail.copy(
                room = cache[detail.roomId]
            )
        }
        return booking.copy(details = enrichedDetails)
    }
}

data class BookingSearchForm(
    val bookingCode: String = "",
    val guestDni: String = ""
)