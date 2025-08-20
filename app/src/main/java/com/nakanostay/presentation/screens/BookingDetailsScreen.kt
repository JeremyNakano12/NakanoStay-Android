package com.nakanostay.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nakanostay.data.models.Booking
import com.nakanostay.data.models.BookingStatus
import com.nakanostay.data.models.Hotel
import com.nakanostay.presentation.viewmodels.AdminBookingsViewModel
import com.nakanostay.ui.theme.OnSurfaceLight
import com.nakanostay.ui.theme.ErrorRed
import com.nakanostay.ui.theme.PrimaryPurple
import com.nakanostay.ui.theme.SuccessGreen
import com.nakanostay.ui.theme.InfoBlue
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    viewModel: AdminBookingsViewModel,
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val booking = uiState.selectedBooking

    if (booking == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = ErrorRed
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Error: No se encontró la reserva",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBackClick) {
                    Text("Volver")
                }
            }
        }
        return
    }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BookingDetailsTopBar(
                bookingCode = booking.bookingCode,
                onBackClick = onBackClick,
                onLogoutClick = { showLogoutDialog = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StatusCard(booking = booking, viewModel = viewModel)
                }

                item {
                    GuestInformationCard(booking = booking)
                }

                item {
                    BookingDatesCard(booking = booking)
                }

                viewModel.getHotelForBooking(booking)?.let { hotel ->
                    item {
                        HotelInformationCard(hotel = hotel)
                    }
                }

                item {
                    RoomsDetailsCard(booking = booking)
                }

                item {
                    ActionButtons(
                        booking = booking,
                        isConfirming = uiState.isConfirming,
                        isCancelling = uiState.isCancelling,
                        isCompleting = uiState.isCompleting,
                        onConfirmClick = { showConfirmDialog = true },
                        onCancelClick = { showCancelDialog = true },
                        onCompleteClick = { showCompleteDialog = true }
                    )
                }
            }

            uiState.errorMessage?.let { error ->
                LaunchedEffect(error) {
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Está seguro que desea cerrar la sesión de administrador?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Confirmar", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar Reserva") },
            text = { Text("¿Está seguro que desea confirmar esta reserva? Se enviará un correo de confirmación al huésped.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmBooking(booking.bookingCode, booking.guestDni)
                        showConfirmDialog = false
                    }
                ) {
                    Text("Confirmar", color = SuccessGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar Reserva") },
            text = { Text("¿Está seguro que desea cancelar esta reserva? Esta acción no se puede deshacer y se notificará al huésped.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelBooking(booking.bookingCode, booking.guestDni)
                        showCancelDialog = false
                    }
                ) {
                    Text("Cancelar Reserva", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Volver")
                }
            }
        )
    }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Completar Reserva") },
            text = { Text("¿Está seguro que desea marcar esta reserva como completada? Esta acción indica que el huésped ya realizó el check-out.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.completeBooking(booking.bookingCode, booking.guestDni)
                        showCompleteDialog = false
                    }
                ) {
                    Text("Completar", color = InfoBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingDetailsTopBar(
    bookingCode: String,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PrimaryPurple,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Detalles de Reserva",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = bookingCode,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Cerrar Sesión",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    booking: Booking,
    viewModel: AdminBookingsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Estado de la Reserva",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = booking.bookingCode,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight.copy(alpha = 0.7f)
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = viewModel.getBookingStatusColor(booking.status).copy(alpha = 0.1f)
            ) {
                Text(
                    text = viewModel.getBookingStatusText(booking.status),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = viewModel.getBookingStatusColor(booking.status),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GuestInformationCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Información del Huésped",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceLight
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(
                icon = Icons.Default.Person,
                label = "Nombre Completo",
                value = booking.guestName
            )

            InfoRow(
                icon = Icons.Default.Badge,
                label = "Cédula de Identidad",
                value = booking.guestDni
            )

            InfoRow(
                icon = Icons.Default.Email,
                label = "Correo Electrónico",
                value = booking.guestEmail
            )

            booking.guestPhone?.let { phone ->
                InfoRow(
                    icon = Icons.Default.Phone,
                    label = "Teléfono",
                    value = phone
                )
            }
        }
    }
}

@Composable
private fun BookingDatesCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Fechas de Estadía",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceLight
            )

            Spacer(modifier = Modifier.height(16.dp))

            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            InfoRow(
                icon = Icons.Default.Login,
                label = "Check-in",
                value = booking.checkIn.format(dateFormatter)
            )

            InfoRow(
                icon = Icons.Default.Logout,
                label = "Check-out",
                value = booking.checkOut.format(dateFormatter)
            )

            InfoRow(
                icon = Icons.Default.CalendarToday,
                label = "Fecha de Reserva",
                value = booking.bookingDate.format(dateFormatter)
            )

            val nights = ChronoUnit.DAYS.between(booking.checkIn, booking.checkOut)
            InfoRow(
                icon = Icons.Default.Hotel,
                label = "Noches de Estadía",
                value = "$nights ${if (nights == 1L) "noche" else "noches"}"
            )
        }
    }
}

@Composable
private fun HotelInformationCard(
    hotel: Hotel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Información del Hotel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceLight
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(
                icon = Icons.Default.Hotel,
                label = "Nombre",
                value = hotel.name
            )

            InfoRow(
                icon = Icons.Default.LocationOn,
                label = "Dirección",
                value = hotel.address
            )

            hotel.city?.let { city ->
                InfoRow(
                    icon = Icons.Default.LocationCity,
                    label = "Ciudad",
                    value = city
                )
            }

            InfoRow(
                icon = Icons.Default.Star,
                label = "Categoría",
                value = hotel.stars?.let { stars ->
                    "${"★".repeat(stars)} ($stars estrella${if (stars > 1) "s" else ""})"
                } ?: "Sin categoría"
            )

            InfoRow(
                icon = Icons.Default.Email,
                label = "Email del Hotel",
                value = hotel.email
            )
        }
    }
}

@Composable
private fun RoomsDetailsCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Habitaciones Reservadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceLight
            )

            Spacer(modifier = Modifier.height(16.dp))

            booking.details.forEachIndexed { index, detail ->
                if (index > 0) {
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = OnSurfaceLight.copy(alpha = 0.1f)
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Habitación ${index + 1}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryPurple
                        )
                        Text(
                            text = "${detail.guests} huésped${if (detail.guests > 1) "es" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceLight.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    detail.room?.let { room ->
                        InfoRow(
                            icon = Icons.Default.Bed,
                            label = "Número",
                            value = room.roomNumber,
                            compact = true
                        )

                        InfoRow(
                            icon = Icons.Default.Category,
                            label = "Tipo",
                            value = room.roomType ?: "No especificado",
                            compact = true
                        )

                        InfoRow(
                            icon = Icons.Default.AttachMoney,
                            label = "Precio por noche",
                            value = "$${room.pricePerNight}",
                            compact = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = OnSurfaceLight.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            val totalGuests = booking.details.sumOf { it.guests }
            val nights = ChronoUnit.DAYS.between(booking.checkIn, booking.checkOut)
            val totalPrice = booking.details.sumOf { detail ->
                detail.room?.pricePerNight?.multiply(nights.toBigDecimal()) ?: BigDecimal.ZERO
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total de huéspedes:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$totalGuests",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total estimado:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$$totalPrice",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    booking: Booking,
    isConfirming: Boolean,
    isCancelling: Boolean,
    isCompleting: Boolean,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Acciones Disponibles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceLight
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (booking.status) {
                BookingStatus.PENDING -> {
                    Button(
                        onClick = onConfirmClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isConfirming && !isCancelling && !isCompleting,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        if (isConfirming) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar Reserva")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isConfirming && !isCancelling && !isCompleting,
                        border = BorderStroke(1.dp, ErrorRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        if (isCancelling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = ErrorRed
                            )
                        } else {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancelar Reserva")
                    }
                }

                BookingStatus.CONFIRMED -> {
                    Button(
                        onClick = onCompleteClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isConfirming && !isCancelling && !isCompleting,
                        colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
                    ) {
                        if (isCompleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Marcar como Completada")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isConfirming && !isCancelling && !isCompleting,
                        border = BorderStroke(1.dp, ErrorRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        if (isCancelling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = ErrorRed
                            )
                        } else {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancelar Reserva")
                    }
                }

                BookingStatus.CANCELLED -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ErrorRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                tint = ErrorRed
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Esta reserva ha sido cancelada. No se pueden realizar más acciones.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ErrorRed
                            )
                        }
                    }
                }

                BookingStatus.COMPLETED -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = InfoBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = InfoBlue
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Esta reserva está completada. El huésped ya realizó el check-out.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = InfoBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    compact: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = OnSurfaceLight.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                color = OnSurfaceLight,
                fontWeight = FontWeight.Normal
            )
        }
    }
}