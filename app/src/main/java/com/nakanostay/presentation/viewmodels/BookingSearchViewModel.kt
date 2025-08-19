package com.nakanostay.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakanostay.data.models.*
import com.nakanostay.data.repository.BookingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookingSearchViewModel(
    private val bookingRepository: BookingRepository
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
                    _bookingState.value = UiState(data = booking)
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
                    // Update the main booking state with cancelled booking
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

    // Helper functions for UI
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
}

data class BookingSearchForm(
    val bookingCode: String = "",
    val guestDni: String = ""
)