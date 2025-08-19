package com.nakanostay.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nakanostay.data.models.BookingFormField
import com.nakanostay.data.models.Hotel
import com.nakanostay.data.models.RoomWithHotel
import com.nakanostay.data.models.UiState
import com.nakanostay.data.models.RoomAvailability
import com.nakanostay.data.models.Booking
import com.nakanostay.data.models.Room
import com.nakanostay.presentation.viewmodels.RoomDetailViewModel
import com.nakanostay.ui.theme.AccentPurple
import com.nakanostay.ui.theme.ErrorRed
import com.nakanostay.ui.theme.LightPink
import com.nakanostay.ui.theme.OnSurfaceLight
import com.nakanostay.ui.theme.PrimaryPurple
import com.nakanostay.ui.theme.SuccessGreen
import com.nakanostay.ui.theme.PrimaryPink
import com.nakanostay.ui.theme.InfoBlue
import com.nakanostay.ui.theme.SecondaryPink
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    roomWithHotel: RoomWithHotel,
    viewModel: RoomDetailViewModel,
    onBackClick: () -> Unit
) {
    val availabilityState by viewModel.availabilityState.collectAsStateWithLifecycle()
    val bookingState by viewModel.bookingState.collectAsStateWithLifecycle()
    val selectedCheckIn by viewModel.selectedCheckIn.collectAsStateWithLifecycle()
    val selectedCheckOut by viewModel.selectedCheckOut.collectAsStateWithLifecycle()
    val showBookingDialog by viewModel.showBookingDialog.collectAsStateWithLifecycle()

    LaunchedEffect(roomWithHotel) {
        viewModel.setRoomWithHotel(roomWithHotel)
        // Load availability for next 3 months
        val startDate = LocalDate.now()
        val endDate = startDate.plusMonths(3)
        viewModel.loadRoomAvailability(startDate, endDate)
    }

    LaunchedEffect(bookingState.data) {
        if (bookingState.data != null) {
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPink)
    ) {
        TopAppBar(
            title = { Text("Detalles de Habitación") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PrimaryPurple,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RoomInfoCard(roomWithHotel = roomWithHotel)

            HotelInfoCard(hotel = roomWithHotel.hotel)

            DateSelectionCard(
                viewModel = viewModel,
                availabilityState = availabilityState,
                selectedCheckIn = selectedCheckIn,
                selectedCheckOut = selectedCheckOut
            )

            if (selectedCheckIn != null && selectedCheckOut != null) {
                BookingSummaryCard(
                    room = roomWithHotel.room,
                    checkIn = selectedCheckIn!!,
                    checkOut = selectedCheckOut!!,
                    onBookNowClick = viewModel::showBookingDialog
                )
            }
        }
    }

    if (showBookingDialog) {
        BookingDialog(
            viewModel = viewModel,
            roomWithHotel = roomWithHotel,
            bookingState = bookingState,
            onDismiss = viewModel::hideBookingDialog
        )
    }

    bookingState.data?.let { booking ->
        BookingSuccessDialog(
            booking = booking,
            onDismiss = {
                viewModel.clearBookingState()
                onBackClick()
            }
        )
    }
}

@Composable
private fun RoomInfoCard(roomWithHotel: RoomWithHotel) {
    val room = roomWithHotel.room

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Habitación ${room.roomNumber}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentPurple
                    )

                    room.roomType?.let { type ->
                        Text(
                            text = type,
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurfaceLight.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    color = if (room.isAvailable) SuccessGreen else ErrorRed,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (room.isAvailable) "Disponible" else "No disponible",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$${room.pricePerNight}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = AccentPurple
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "por noche",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceLight.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun HotelInfoCard(hotel: Hotel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Información del Hotel",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AccentPurple
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hotel.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                hotel.stars?.let { stars ->
                    Row {
                        repeat(stars) {
                            Text(text = "⭐", fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryPink,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = hotel.address,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    hotel.city?.let { city ->
                        Text(
                            text = city,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceLight.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = PrimaryPink,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = hotel.email,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DateSelectionCard(
    viewModel: RoomDetailViewModel,
    availabilityState: UiState<RoomAvailability>,
    selectedCheckIn: LocalDate?,
    selectedCheckOut: LocalDate?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Seleccionar Fechas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AccentPurple
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                availabilityState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                }

                availabilityState.error != null -> {
                    Text(
                        text = "Error al cargar disponibilidad: ${availabilityState.error}",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> {
                    SimpleDatePicker(
                        viewModel = viewModel,
                        selectedCheckIn = selectedCheckIn,
                        selectedCheckOut = selectedCheckOut
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleDatePicker(
    viewModel: RoomDetailViewModel,
    selectedCheckIn: LocalDate?,
    selectedCheckOut: LocalDate?
) {
    var showCheckInPicker by remember { mutableStateOf(false) }
    var showCheckOutPicker by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(
            onClick = { showCheckInPicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCheckIn?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        ?: "Seleccionar fecha de entrada"
                )
                Icon(Icons.Default.DateRange, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { showCheckOutPicker = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedCheckIn != null
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCheckOut?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        ?: "Seleccionar fecha de salida"
                )
                Icon(Icons.Default.DateRange, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            color = InfoBlue.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "💡 Solo se muestran las fechas disponibles para reserva",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = InfoBlue
            )
        }
    }

    if (showCheckInPicker) {
        SimpleDatePickerDialog(
            title = "Fecha de Entrada",
            onDateSelected = { date ->
                viewModel.selectCheckInDate(date)
                showCheckInPicker = false
            },
            onDismiss = { showCheckInPicker = false },
            viewModel = viewModel,
            isCheckOut = false
        )
    }

    if (showCheckOutPicker) {
        SimpleDatePickerDialog(
            title = "Fecha de Salida",
            onDateSelected = { date ->
                viewModel.selectCheckOutDate(date)
                showCheckOutPicker = false
            },
            onDismiss = { showCheckOutPicker = false },
            viewModel = viewModel,
            isCheckOut = true
        )
    }
}

@Composable
private fun SimpleDatePickerDialog(
    title: String,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    viewModel: RoomDetailViewModel,
    isCheckOut: Boolean
) {
    val availableDates by viewModel.availableDates.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                val today = LocalDate.now()
                val availableDatesToShow = if (isCheckOut) {
                    availableDates.filter { date -> viewModel.canSelectCheckOutDate(date) }
                } else {
                    availableDates.filter { date -> date >= today }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (availableDatesToShow.isEmpty()) {
                        Text(
                            text = "No hay fechas disponibles",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceLight.copy(alpha = 0.7f)
                        )
                    } else {
                        Column {

                            availableDatesToShow.chunked(2).forEach { dateRow ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    dateRow.forEach { date ->
                                        OutlinedButton(
                                            onClick = { onDateSelected(date) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = date.format(DateTimeFormatter.ofPattern("dd/MM")),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    if (dateRow.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
private fun BookingSummaryCard(
    room: Room,
    checkIn: LocalDate,
    checkOut: LocalDate,
    onBookNowClick: () -> Unit
) {
    val nights = ChronoUnit.DAYS.between(checkIn, checkOut).toInt()
    val totalPrice = room.pricePerNight.multiply(nights.toBigDecimal())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SecondaryPink),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Resumen de Reserva",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AccentPurple
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Entrada:")
                Text(
                    text = checkIn.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Salida:")
                Text(
                    text = checkOut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    fontWeight = FontWeight.Medium
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$nights noche${if (nights > 1) "s" else ""} × ${room.pricePerNight}")
                Text("${totalPrice}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${totalPrice}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentPurple
                    )
                }

                Button(
                    onClick = onBookNowClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) {
                    Text("Reservar Ahora")
                }
            }
        }
    }
}

@Composable
private fun BookingDialog(
    viewModel: RoomDetailViewModel,
    roomWithHotel: RoomWithHotel,
    bookingState: UiState<Booking>,
    onDismiss: () -> Unit
) {
    val bookingForm by viewModel.bookingForm.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Crear Reserva",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AccentPurple
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bookingForm.guestName,
                    onValueChange = { viewModel.updateBookingFormField(BookingFormField.GUEST_NAME, it) },
                    label = { Text("Nombre completo *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bookingForm.guestDni,
                    onValueChange = { input ->
                        if (input.length <= 10 && input.all { it.isDigit() }) {
                            viewModel.updateBookingFormField(BookingFormField.GUEST_DNI, input)
                        }
                    },
                    label = { Text("Cédula *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("1234567890") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bookingForm.guestEmail,
                    onValueChange = { viewModel.updateBookingFormField(BookingFormField.GUEST_EMAIL, it) },
                    label = { Text("Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("ejemplo@correo.com") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bookingForm.guestPhone,
                    onValueChange = { input ->
                        if (input.length <= 13 && input.all { it.isDigit() }) {
                            viewModel.updateBookingFormField(BookingFormField.GUEST_PHONE, input)
                        }
                    },
                    label = { Text("Teléfono (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("0999999999") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bookingForm.guests.toString(),
                    onValueChange = { viewModel.updateBookingFormField(BookingFormField.GUESTS, it) },
                    label = { Text("Número de huéspedes *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = LightPink,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Resumen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Habitación: ${roomWithHotel.room.roomNumber}")
                        Text("Hotel: ${roomWithHotel.hotel.name}")

                        bookingForm.checkIn?.let { checkIn ->
                            bookingForm.checkOut?.let { checkOut ->
                                val nights = ChronoUnit.DAYS.between(checkIn, checkOut).toInt()
                                val total = roomWithHotel.room.pricePerNight.multiply(nights.toBigDecimal())

                                Text("Fechas: ${checkIn.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} - ${checkOut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                                Text("$nights noche${if (nights > 1) "s" else ""}")
                                Text(
                                    text = "Total: $total",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentPurple
                                )
                            }
                        }
                    }
                }

                if (bookingState.error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = bookingState.error,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !bookingState.isLoading
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = viewModel::createBooking,
                        modifier = Modifier.weight(1f),
                        enabled = !bookingState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        if (bookingState.isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Text("Creando...")
                            }
                        } else {
                            Text("Reservar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingSuccessDialog(
    booking: Booking,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = SuccessGreen,
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¡Reserva Exitosa!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tu reserva ha sido creada correctamente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = LightPink,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Código de Reserva",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = booking.bookingCode,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentPurple
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Huésped: ${booking.guestName}")
                        Text("Email: ${booking.guestEmail}")
                        Text("Check-in: ${booking.checkIn.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                        Text("Check-out: ${booking.checkOut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                        Text("Total: ${booking.total}")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Guarda este código para consultar tu reserva más tarde",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceLight.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) {
                    Text("Entendido")
                }
            }
        }
    }
}