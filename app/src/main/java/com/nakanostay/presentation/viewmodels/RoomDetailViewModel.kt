package com.nakanostay.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakanostay.data.models.BookingFormField
import com.nakanostay.data.models.Booking
import com.nakanostay.data.models.BookingRequest
import com.nakanostay.data.models.BookingDetailRequest
import com.nakanostay.data.models.RoomAvailability
import com.nakanostay.data.models.RoomWithHotel
import com.nakanostay.data.models.UiState
import com.nakanostay.data.repository.BookingRepository
import com.nakanostay.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RoomDetailViewModel(
    private val roomRepository: RoomRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _roomWithHotel = MutableStateFlow<RoomWithHotel?>(null)
    val roomWithHotel: StateFlow<RoomWithHotel?> = _roomWithHotel.asStateFlow()

    private val _availabilityState = MutableStateFlow(UiState<RoomAvailability>())
    val availabilityState: StateFlow<UiState<RoomAvailability>> = _availabilityState.asStateFlow()

    private val _bookingState = MutableStateFlow(UiState<Booking>())
    val bookingState: StateFlow<UiState<Booking>> = _bookingState.asStateFlow()

    private val _selectedCheckIn = MutableStateFlow<LocalDate?>(null)
    val selectedCheckIn: StateFlow<LocalDate?> = _selectedCheckIn.asStateFlow()

    private val _selectedCheckOut = MutableStateFlow<LocalDate?>(null)
    val selectedCheckOut: StateFlow<LocalDate?> = _selectedCheckOut.asStateFlow()

    private val _availableDates = MutableStateFlow<List<LocalDate>>(emptyList())
    val availableDates: StateFlow<List<LocalDate>> = _availableDates.asStateFlow()

    private val _bookingForm = MutableStateFlow(BookingFormData())
    val bookingForm: StateFlow<BookingFormData> = _bookingForm.asStateFlow()

    private val _showBookingDialog = MutableStateFlow(false)
    val showBookingDialog: StateFlow<Boolean> = _showBookingDialog.asStateFlow()

    private val _dniValidationState = MutableStateFlow(DniValidationState())
    val dniValidationState: StateFlow<DniValidationState> = _dniValidationState.asStateFlow()

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
            BookingFormField.GUEST_DNI -> {
                validateDniInRealTime(value)
                currentForm.copy(guestDni = value)
            }
            BookingFormField.GUEST_EMAIL -> currentForm.copy(guestEmail = value)
            BookingFormField.GUEST_PHONE -> currentForm.copy(guestPhone = value)
            BookingFormField.GUESTS -> {
                val guests = value.toIntOrNull() ?: 1
                currentForm.copy(guests = guests)
            }
        }
        _bookingForm.value = updatedForm
    }

    private fun validateDniInRealTime(dni: String) {
        when {
            dni.isEmpty() -> {
                _dniValidationState.value = DniValidationState()
            }
            dni.length < 10 -> {
                _dniValidationState.value = DniValidationState(
                    isValidating = false,
                    isValid = false,
                    errorMessage = "La cédula debe tener 10 dígitos"
                )
            }
            dni.length == 10 -> {
                val validationResult = validateEcuadorianDni(dni)
                _dniValidationState.value = DniValidationState(
                    isValidating = false,
                    isValid = validationResult.isValid,
                    errorMessage = validationResult.errorMessage
                )
            }
            else -> {
                _dniValidationState.value = DniValidationState(
                    isValidating = false,
                    isValid = false,
                    errorMessage = "La cédula no puede tener más de 10 dígitos"
                )
            }
        }
    }

    private fun validateEcuadorianDni(dni: String): DniValidationResult {
        if (!dni.matches("^[0-9]{10}$".toRegex())) {
            return DniValidationResult(false, "La cédula debe contener solo números")
        }

        val province = dni.substring(0, 2).toInt()
        if (province < 1 || province > 24) {
            return DniValidationResult(false, "Código de provincia inválido")
        }

        val thirdDigit = dni[2].toString().toInt()
        if (thirdDigit > 6) {
            return DniValidationResult(false, "Tercer dígito inválido para persona natural")
        }

        val coefficients = intArrayOf(2, 1, 2, 1, 2, 1, 2, 1, 2)
        var sum = 0

        for (i in 0..8) {
            var product = dni[i].toString().toInt() * coefficients[i]
            if (product > 9) {
                product -= 9
            }
            sum += product
        }

        val verifierDigit = dni[9].toString().toInt()
        val calculatedDigit = if (sum % 10 == 0) 0 else 10 - (sum % 10)

        return if (verifierDigit == calculatedDigit) {
            DniValidationResult(true, null)
        } else {
            DniValidationResult(false, "Cédula ecuatoriana inválida")
        }
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

data class DniValidationState(
    val isValidating: Boolean = false,
    val isValid: Boolean? = null,
    val errorMessage: String? = null
)

private data class DniValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)