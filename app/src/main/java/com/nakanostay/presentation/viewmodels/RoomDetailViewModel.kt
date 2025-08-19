package com.nakanostay.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakanostay.data.models.*
import com.nakanostay.data.repository.BookingRepository
import com.nakanostay.data.repository.RoomRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RoomDetailViewModel(
    private val roomRepository: RoomRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    // Current room with hotel
    private val _roomWithHotel = MutableStateFlow<RoomWithHotel?>(null)
    val roomWithHotel: StateFlow<RoomWithHotel?> = _roomWithHotel.asStateFlow()

    // Room availability state
    private val _availabilityState = MutableStateFlow(UiState<RoomAvailability>())
    val availabilityState: StateFlow<UiState<RoomAvailability>> = _availabilityState.asStateFlow()

    // Booking creation state
    private val _bookingState = MutableStateFlow(UiState<Booking>())
    val bookingState: StateFlow<UiState<Booking>> = _bookingState.asStateFlow()

    // Selected dates for booking
    private val _selectedCheckIn = MutableStateFlow<LocalDate?>(null)
    val selectedCheckIn: StateFlow<LocalDate?> = _selectedCheckIn.asStateFlow()

    private val _selectedCheckOut = MutableStateFlow<LocalDate?>(null)
    val selectedCheckOut: StateFlow<LocalDate?> = _selectedCheckOut.asStateFlow()

    // Available dates for selection
    private val _availableDates = MutableStateFlow<List<LocalDate>>(emptyList())
    val availableDates: StateFlow<List<LocalDate>> = _availableDates.asStateFlow()

    // Booking form data
    private val _bookingForm = MutableStateFlow(BookingFormData())
    val bookingForm: StateFlow<BookingFormData> = _bookingForm.asStateFlow()

    // Show booking dialog state
    private val _showBookingDialog = MutableStateFlow(false)
    val showBookingDialog: StateFlow<Boolean> = _showBookingDialog.asStateFlow()

    fun setRoomWithHotel(roomWithHotel: RoomWithHotel) {
        _roomWithHotel.value = roomWithHotel
    }

    fun loadRoomAvailability(startDate: LocalDate, endDate: LocalDate) {
        val room = _roomWithHotel.value?.room ?: return

        viewModelScope.launch {
            _availabilityState.value = UiState(isLoading = true)

            val startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

            roomRepository.getRoomAvailability(room.id, startDateStr, endDateStr).collect { result ->
                if (result.isSuccess) {
                    val availability = result.getOrNull()
                    _availabilityState.value = UiState(data = availability)

                    // Convert available date strings to LocalDate objects
                    availability?.let { avail ->
                        val dates = avail.availableDates.map { dateStr ->
                            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                        }
                        _availableDates.value = dates
                    }
                } else {
                    _availabilityState.value = UiState(
                        error = result.exceptionOrNull()?.message ?: "Error loading availability"
                    )
                }
            }
        }
    }

    fun selectCheckInDate(date: LocalDate) {
        _selectedCheckIn.value = date
        // Clear check-out if it's before or same as check-in
        _selectedCheckOut.value?.let { checkOut ->
            if (checkOut <= date) {
                _selectedCheckOut.value = null
            }
        }
        updateBookingForm()
    }

    fun selectCheckOutDate(date: LocalDate) {
        _selectedCheckOut.value = date
        updateBookingForm()
    }

    fun updateBookingFormField(field: BookingFormField, value: String) {
        val currentForm = _bookingForm.value
        val updatedForm = when (field) {
            BookingFormField.GUEST_NAME -> currentForm.copy(guestName = value)
            BookingFormField.GUEST_DNI -> currentForm.copy(guestDni = value)
            BookingFormField.GUEST_EMAIL -> currentForm.copy(guestEmail = value)
            BookingFormField.GUEST_PHONE -> currentForm.copy(guestPhone = value)
            BookingFormField.GUESTS -> {
                val guests = value.toIntOrNull() ?: 1
                currentForm.copy(guests = guests)
            }
        }
        _bookingForm.value = updatedForm
    }

    private fun updateBookingForm() {
        val checkIn = _selectedCheckIn.value
        val checkOut = _selectedCheckOut.value
        if (checkIn != null && checkOut != null) {
            _bookingForm.value = _bookingForm.value.copy(
                checkIn = checkIn,
                checkOut = checkOut
            )
        }
    }

    fun showBookingDialog() {
        _showBookingDialog.value = true
    }

    fun hideBookingDialog() {
        _showBookingDialog.value = false
    }

    fun createBooking() {
        val room = _roomWithHotel.value?.room ?: return
        val form = _bookingForm.value

        if (!isFormValid(form)) {
            _bookingState.value = UiState(error = "Por favor completa todos los campos requeridos")
            return
        }

        viewModelScope.launch {
            _bookingState.value = UiState(isLoading = true)

            val bookingRequest = BookingRequest(
                guestName = form.guestName,
                guestDni = form.guestDni,
                guestEmail = form.guestEmail,
                guestPhone = form.guestPhone.takeIf { it.isNotBlank() },
                checkIn = form.checkIn!!,
                checkOut = form.checkOut!!,
                details = listOf(
                    BookingDetailRequest(
                        roomId = room.id,
                        guests = form.guests
                    )
                )
            )

            bookingRepository.createBooking(bookingRequest).collect { result ->
                if (result.isSuccess) {
                    val booking = result.getOrNull()
                    _bookingState.value = UiState(data = booking)
                    // Clear form after successful booking
                    clearBookingForm()
                } else {
                    _bookingState.value = UiState(
                        error = result.exceptionOrNull()?.message ?: "Error creating booking"
                    )
                }
            }
        }
    }

    private fun isFormValid(form: BookingFormData): Boolean {
        return form.guestName.isNotBlank() &&
                form.guestDni.isNotBlank() &&
                form.guestEmail.isNotBlank() &&
                form.checkIn != null &&
                form.checkOut != null &&
                form.guests > 0 &&
                isValidEmail(form.guestEmail) &&
                isValidDni(form.guestDni)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidDni(dni: String): Boolean {
        return dni.matches("^[0-9]{10}$".toRegex())
    }

    fun clearBookingForm() {
        _bookingForm.value = BookingFormData()
        _selectedCheckIn.value = null
        _selectedCheckOut.value = null
        _showBookingDialog.value = false
    }

    fun clearBookingState() {
        _bookingState.value = UiState()
    }

    fun getSelectedDateRange(): Pair<LocalDate, LocalDate>? {
        val checkIn = _selectedCheckIn.value
        val checkOut = _selectedCheckOut.value
        return if (checkIn != null && checkOut != null) {
            Pair(checkIn, checkOut)
        } else null
    }

    fun isDateAvailable(date: LocalDate): Boolean {
        return _availableDates.value.contains(date)
    }

    fun canSelectCheckOutDate(date: LocalDate): Boolean {
        val checkIn = _selectedCheckIn.value ?: return false
        return date > checkIn && isDateAvailable(date)
    }
}

// Data classes for booking form
data class BookingFormData(
    val guestName: String = "",
    val guestDni: String = "",
    val guestEmail: String = "",
    val guestPhone: String = "",
    val checkIn: LocalDate? = null,
    val checkOut: LocalDate? = null,
    val guests: Int = 1
)