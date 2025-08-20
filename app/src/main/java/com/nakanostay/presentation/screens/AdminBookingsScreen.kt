package com.nakanostay.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nakanostay.data.models.Booking
import com.nakanostay.data.models.BookingStatus
import com.nakanostay.presentation.viewmodels.AdminBookingsViewModel
import com.nakanostay.ui.theme.ErrorRed
import com.nakanostay.ui.theme.PrimaryPurple
import com.nakanostay.ui.theme.OnSurfaceLight
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingsScreen(
    viewModel: AdminBookingsViewModel,
    onBackClick: () -> Unit,
    onBookingClick: (Booking) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AdminBookingsTopBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                selectedStatus = uiState.selectedStatus,
                availableStatuses = uiState.availableStatuses,
                onStatusSelected = viewModel::updateSelectedStatus,
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
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                uiState.errorMessage != null -> {
                    ErrorState(
                        error = uiState.errorMessage!!,
                        onRetry = viewModel::refreshBookings
                    )
                }
                uiState.filteredBookings.isEmpty() && uiState.bookings.isNotEmpty() -> {
                    EmptySearchState()
                }
                uiState.bookings.isEmpty() -> {
                    EmptyBookingsState()
                }
                else -> {
                    BookingsList(
                        bookings = uiState.filteredBookings,
                        viewModel = viewModel,
                        onBookingClick = onBookingClick,
                        totalCount = uiState.bookings.size,
                        filteredCount = uiState.filteredBookings.size
                    )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminBookingsTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedStatus: BookingStatus?,
    availableStatuses: List<BookingStatus>,
    onStatusSelected: (BookingStatus?) -> Unit,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showStatusFilter by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PrimaryPurple,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = "Gestión de Reservas",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Cerrar Sesión",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por código de reserva, cédula o nombre...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                    focusedLeadingIconColor = Color.White,
                    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTrailingIconColor = Color.White,
                    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f)
                )
            )

            if (selectedStatus != null || searchQuery.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedStatus != null) {
                        item {
                            FilterChip(
                                onClick = { onStatusSelected(null) },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Estado: ${getStatusText(selectedStatus)}")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Quitar filtro",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                selected = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box {
                FilterChip(
                    onClick = { showStatusFilter = true },
                    label = {
                        Text(
                            selectedStatus?.let { "Estado: ${getStatusText(it)}" } ?: "Todos los estados",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (selectedStatus != null) {
                                Color.Black
                            } else {
                                Color.White.copy(alpha = 0.7f)
                            }

                        )
                    },
                    selected = selectedStatus != null,
                    leadingIcon = {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            if (selectedStatus != null) {
                                Color.Black
                            } else {
                                Color.White.copy(alpha = 0.7f)
                            }

                        )
                    },
                    border = FilterChipDefaults.filterChipBorder(
                        selectedBorderColor = Color.White,
                        selected = true,
                        enabled = true
                    ),
                )

                DropdownMenu(
                    expanded = showStatusFilter,
                    onDismissRequest = { showStatusFilter = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos los estados") },
                        onClick = {
                            onStatusSelected(null)
                            showStatusFilter = false
                        },
                        leadingIcon = if (selectedStatus == null) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )

                    availableStatuses.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(getStatusText(status)) },
                            onClick = {
                                onStatusSelected(status)
                                showStatusFilter = false
                            },
                            leadingIcon = if (selectedStatus == status) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingsList(
    bookings: List<Booking>,
    viewModel: AdminBookingsViewModel,
    onBookingClick: (Booking) -> Unit,
    totalCount: Int,
    filteredCount: Int
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mostrando $filteredCount de $totalCount reservas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight.copy(alpha = 0.7f)
                )
            }
        }

        items(bookings) { booking ->
            BookingCard(
                booking = booking,
                viewModel = viewModel,
                onBookingClick = onBookingClick
            )
        }
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    viewModel: AdminBookingsViewModel,
    onBookingClick: (Booking) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBookingClick(booking) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.bookingCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = viewModel.getBookingStatusColor(booking.status).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = viewModel.getBookingStatusText(booking.status),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = viewModel.getBookingStatusColor(booking.status),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = OnSurfaceLight.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${booking.guestName} • ${booking.guestDni}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = OnSurfaceLight.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                Text(
                    text = "${booking.checkIn.format(dateFormatter)} - ${booking.checkOut.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            viewModel.getHotelForBooking(booking)?.let { hotel ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Hotel,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = OnSurfaceLight.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = hotel.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bed,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = OnSurfaceLight.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${booking.details.size} habitación${if (booking.details.size > 1) "es" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceLight.copy(alpha = 0.8f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver detalles",
                    tint = OnSurfaceLight.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun EmptyBookingsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = OnSurfaceLight.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay reservas registradas",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceLight.copy(alpha = 0.6f)
            )
            Text(
                text = "Las reservas creadas aparecerán aquí",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun EmptySearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = OnSurfaceLight.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No se encontraron reservas",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceLight.copy(alpha = 0.6f)
            )
            Text(
                text = "Intenta con otros términos de búsqueda",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PrimaryPurple)
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = ErrorRed
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error al cargar reservas",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceLight.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Reintentar")
            }
        }
    }
}

private fun getStatusText(status: BookingStatus): String {
    return when (status) {
        BookingStatus.PENDING -> "Pendiente"
        BookingStatus.CONFIRMED -> "Confirmada"
        BookingStatus.CANCELLED -> "Cancelada"
        BookingStatus.COMPLETED -> "Completada"
    }
}