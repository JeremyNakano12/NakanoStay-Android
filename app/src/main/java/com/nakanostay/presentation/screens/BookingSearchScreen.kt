package com.nakanostay.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nakanostay.data.models.Booking
import com.nakanostay.data.models.BookingStatus
import com.nakanostay.data.models.UiState
import com.nakanostay.presentation.viewmodels.BookingSearchForm
import com.nakanostay.presentation.viewmodels.BookingSearchViewModel
import com.nakanostay.ui.theme.AccentPurple
import com.nakanostay.ui.theme.OnSurfaceLight
import com.nakanostay.ui.theme.ErrorRed
import com.nakanostay.ui.theme.SecondaryPink
import com.nakanostay.ui.theme.LightPink
import com.nakanostay.ui.theme.PrimaryPurple
import com.nakanostay.ui.theme.PrimaryPink
import com.nakanostay.ui.theme.WarningOrange
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSearchScreen(
    viewModel: BookingSearchViewModel
) {
    val searchForm by viewModel.searchForm.collectAsStateWithLifecycle()
    val bookingState by viewModel.bookingState.collectAsStateWithLifecycle()
    val cancellationState by viewModel.cancellationState.collectAsStateWithLifecycle()
    val showCancellationDialog by viewModel.showCancellationDialog.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPink)
    ) {
        TopAppBar(
            title = { Text("Consultar Reserva") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PrimaryPurple,
                titleContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SearchFormCard(
                searchForm = searchForm,
                onBookingCodeChange = viewModel::updateBookingCode,
                onGuestDniChange = viewModel::updateGuestDni,
                onSearchClick = viewModel::searchBooking,
                onClearClick = viewModel::clearSearch,
                isLoading = bookingState.isLoading
            )

            when {
                bookingState.isLoading -> {
                    LoadingCard()
                }

                bookingState.error != null -> {
                    ErrorCard(
                        error = bookingState.error!!,
                        onRetry = viewModel::searchBooking
                    )
                }

                bookingState.data != null -> {
                    BookingResultCard(
                        booking = bookingState.data!!,
                        viewModel = viewModel,
                        onCancelClick = viewModel::showCancellationDialog
                    )
                }
            }
        }
    }

    if (showCancellationDialog) {
        CancellationConfirmationDialog(
            booking = bookingState.data!!,
            cancellationState = cancellationState,
            onConfirm = viewModel::cancelBooking,
            onDismiss = viewModel::hideCancellationDialog
        )
    }
}

@Composable
private fun SearchFormCard(
    searchForm: BookingSearchForm,
    onBookingCodeChange: (String) -> Unit,
    onGuestDniChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onClearClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = AccentPurple,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "Buscar Reserva",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AccentPurple
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ingresa tu código de reserva y cédula para consultar el estado de tu reserva",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = searchForm.bookingCode,
                onValueChange = onBookingCodeChange,
                label = { Text("Código de Reserva") },
                placeholder = { Text("NKS-XXXXXX") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.ConfirmationNumber, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchForm.guestDni,
                onValueChange = onGuestDniChange,
                label = { Text("Cédula") },
                placeholder = { Text("1234567890") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClearClick,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("Limpiar")
                }

                Button(
                    onClick = onSearchClick,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && searchForm.bookingCode.isNotBlank() && searchForm.guestDni.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) {
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text("Buscando...")
                        }
                    } else {
                        Text("Buscar")
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Buscando reserva...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(
    error: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Reserva no encontrada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ErrorRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(onClick = onRetry) {
                Text("Intentar de nuevo")
            }
        }
    }
}

@Composable
private fun BookingResultCard(
    booking: Booking,
    viewModel: BookingSearchViewModel,
    onCancelClick: () -> Unit
) {
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
                        text = "Reserva Encontrada",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AccentPurple
                    )

                    Text(
                        text = booking.bookingCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = viewModel.getBookingStatusColor(booking.status),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = viewModel.getBookingStatusText(booking.status),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BookingInfoSection(
                title = "Información del Huésped",
                icon = Icons.Default.Person
            ) {
                InfoRow("Nombre", booking.guestName)
                InfoRow("Cédula", booking.guestDni)
                InfoRow("Email", booking.guestEmail)
                booking.guestPhone?.let { phone ->
                    InfoRow("Teléfono", phone)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BookingInfoSection(
                title = "Detalles de la Reserva",
                icon = Icons.Default.CalendarMonth
            ) {
                InfoRow("Fecha de reserva", booking.bookingDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                InfoRow("Check-in", booking.checkIn.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                InfoRow("Check-out", booking.checkOut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))

                val nights = ChronoUnit.DAYS.between(booking.checkIn, booking.checkOut).toInt()
                InfoRow("Noches", "$nights noche${if (nights > 1) "s" else ""}")
                InfoRow("Huéspedes", "${booking.details.sumOf { it.guests }}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            BookingInfoSection(
                title = "Habitaciones",
                icon = Icons.Default.Hotel
            ) {
                booking.details.forEach { detail ->
                    Surface(
                        color = LightPink,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Habitación ${detail.room?.roomNumber ?: detail.roomId}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text("${detail.guests} huésped${if (detail.guests > 1) "es" else ""}")
                            Text("Precio: $${detail.priceAtBooking}")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total
            Surface(
                color = SecondaryPink,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$${booking.total}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentPurple
                    )
                }
            }

            if (booking.status == BookingStatus.PENDING || booking.status == BookingStatus.CONFIRMED) {
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar Reserva")
                }
            }
        }
    }
}

@Composable
private fun BookingInfoSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPink,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AccentPurple
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        content()
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceLight.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CancellationConfirmationDialog(
    booking: Booking,
    cancellationState: UiState<Booking>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = "Confirmar Cancelación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¿Estás seguro de que deseas cancelar la reserva ${booking.bookingCode}?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorRed
                )

                if (cancellationState.error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = cancellationState.error,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall
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
                        enabled = !cancellationState.isLoading
                    ) {
                        Text("No, conservar")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        enabled = !cancellationState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        if (cancellationState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sí, cancelar")
                        }
                    }
                }
            }
        }
    }
}