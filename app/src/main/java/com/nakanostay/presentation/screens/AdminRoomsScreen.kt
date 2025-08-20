package com.nakanostay.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nakanostay.data.models.Hotel
import com.nakanostay.data.models.Room
import com.nakanostay.presentation.viewmodels.AdminRoomsViewModel
import com.nakanostay.ui.theme.*
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRoomsScreen(
    viewModel: AdminRoomsViewModel,
    onBackClick: () -> Unit,
    onRoomClick: (Room) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Room?>(null) }

    Scaffold(
        topBar = {
            AdminRoomsTopBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                selectedHotel = uiState.selectedHotel,
                hotels = uiState.availableHotels,
                onHotelSelected = viewModel::updateSelectedHotel,
                selectedRoomType = uiState.selectedRoomType,
                roomTypes = uiState.availableRoomTypes,
                onRoomTypeSelected = viewModel::updateSelectedRoomType,
                onBackClick = onBackClick,
                onLogoutClick = { showLogoutDialog = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setShowCreateDialog(true) },
                containerColor = PrimaryPurple
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear Habitación",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val errorMessage = uiState.errorMessage
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                errorMessage != null -> {
                    ErrorState(
                        error = errorMessage,
                        onRetry = viewModel::refreshRooms
                    )
                }
                uiState.filteredRooms.isEmpty() && uiState.rooms.isNotEmpty() -> {
                    EmptySearchState()
                }
                uiState.rooms.isEmpty() -> {
                    EmptyRoomsState()
                }
                else -> {
                    RoomsList(
                        rooms = uiState.filteredRooms,
                        viewModel = viewModel,
                        onRoomClick = onRoomClick,
                        onEditClick = { room -> viewModel.setEditingRoom(room) },
                        onDeleteClick = { room -> showDeleteDialog = room },
                        onToggleAvailability = viewModel::toggleRoomAvailability,
                        totalCount = uiState.rooms.size,
                        filteredCount = uiState.filteredRooms.size
                    )
                }
            }
        }
    }

    // Create/Edit Room Dialog
    if (uiState.showCreateDialog || uiState.editingRoom != null) {
        CreateEditRoomDialog(
            room = uiState.editingRoom,
            hotels = uiState.availableHotels,
            isLoading = uiState.isCreatingOrUpdating,
            onDismiss = {
                viewModel.setShowCreateDialog(false)
                viewModel.setEditingRoom(null)
            },
            onSave = { hotelId, roomNumber, roomType, pricePerNight, isAvailable ->
                if (uiState.editingRoom != null) {
                    viewModel.updateRoom(
                        uiState.editingRoom!!.id!!,
                        hotelId,
                        roomNumber,
                        roomType,
                        pricePerNight,
                        isAvailable
                    )
                } else {
                    viewModel.createRoom(hotelId, roomNumber, roomType, pricePerNight, isAvailable)
                }
            }
        )
    }

    // Logout Confirmation Dialog
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

    // Delete Confirmation Dialog
    showDeleteDialog?.let { room ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Habitación") },
            text = {
                Text("¿Está seguro que desea eliminar la habitación \"${room.roomNumber}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRoom(room.id!!)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Eliminar", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminRoomsTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedHotel: Hotel?,
    hotels: List<Hotel>,
    onHotelSelected: (Hotel?) -> Unit,
    selectedRoomType: String?,
    roomTypes: List<String>,
    onRoomTypeSelected: (String?) -> Unit,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showHotelFilter by remember { mutableStateOf(false) }
    var showTypeFilter by remember { mutableStateOf(false) }

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
                        text = "Gestión de Habitaciones",
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
                placeholder = { Text("Buscar habitaciones por número...") },
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
                    cursorColor = Color.White,
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                    focusedLeadingIconColor = Color.White,
                    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTrailingIconColor = Color.White,
                    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    FilterChip(
                        onClick = { showHotelFilter = true },
                        label = {
                            Text(
                                selectedHotel?.name ?: "Todos los hoteles",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selected = selectedHotel != null,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Hotel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        //modifier = Modifier.weight(1f)
                    )

                    DropdownMenu(
                        expanded = showHotelFilter,
                        onDismissRequest = { showHotelFilter = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los hoteles") },
                            onClick = {
                                onHotelSelected(null)
                                showHotelFilter = false
                            },
                            leadingIcon = if (selectedHotel == null) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null
                        )

                        hotels.forEach { hotel ->
                            DropdownMenuItem(
                                text = { Text(hotel.name) },
                                onClick = {
                                    onHotelSelected(hotel)
                                    showHotelFilter = false
                                },
                                leadingIcon = if (selectedHotel?.id == hotel.id) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }

                Box {
                    FilterChip(
                        onClick = { showTypeFilter = true },
                        label = {
                            Text(
                                selectedRoomType ?: "Todos los tipos",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selected = selectedRoomType != null,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        //modifier = Modifier.weight(1f)
                    )

                    DropdownMenu(
                        expanded = showTypeFilter,
                        onDismissRequest = { showTypeFilter = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los tipos") },
                            onClick = {
                                onRoomTypeSelected(null)
                                showTypeFilter = false
                            },
                            leadingIcon = if (selectedRoomType == null) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null
                        )

                        roomTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    onRoomTypeSelected(type)
                                    showTypeFilter = false
                                },
                                leadingIcon = if (selectedRoomType == type) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }
            }

            if (selectedHotel != null || selectedRoomType != null || searchQuery.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (searchQuery.isNotEmpty()) {
                        item {
                            FilterChip(
                                onClick = { onSearchQueryChange("") },
                                label = { Text("Búsqueda: $searchQuery") },
                                selected = true,
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Quitar filtro",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }

                    if (selectedHotel != null) {
                        item {
                            FilterChip(
                                onClick = { onHotelSelected(null) },
                                label = { Text("Hotel: ${selectedHotel.name}") },
                                selected = true,
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Quitar filtro",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }

                    if (selectedRoomType != null) {
                        item {
                            FilterChip(
                                onClick = { onRoomTypeSelected(null) },
                                label = { Text("Tipo: $selectedRoomType") },
                                selected = true,
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Quitar filtro",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomsList(
    rooms: List<Room>,
    viewModel: AdminRoomsViewModel,
    onRoomClick: (Room) -> Unit,
    onEditClick: (Room) -> Unit,
    onDeleteClick: (Room) -> Unit,
    onToggleAvailability: (Room) -> Unit,
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
                    text = "Mostrando $filteredCount de $totalCount habitaciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight.copy(alpha = 0.7f)
                )
            }
        }

        items(rooms) { room ->
            RoomCard(
                room = room,
                viewModel = viewModel,
                onRoomClick = onRoomClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onToggleAvailability = onToggleAvailability
            )
        }
    }
}

@Composable
private fun RoomCard(
    room: Room,
    viewModel: AdminRoomsViewModel,
    onRoomClick: (Room) -> Unit,
    onEditClick: (Room) -> Unit,
    onDeleteClick: (Room) -> Unit,
    onToggleAvailability: (Room) -> Unit
) {
    val hotel = viewModel.getHotelById(room.hotelId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRoomClick(room) },
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Habitación ${room.roomNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (room.roomType != null) {
                        Text(
                            text = room.roomType,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceLight.copy(alpha = 0.7f)
                        )
                    }
                }

                Badge(
                    containerColor = if (room.isAvailable) SuccessGreen else ErrorRed,
                    contentColor = Color.White
                ) {
                    Text(
                        text = if (room.isAvailable) "Disponible" else "No disponible",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hotel,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = hotel?.name ?: "Hotel desconocido",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${room.pricePerNight}/noche",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryPurple
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = { onToggleAvailability(room) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (room.isAvailable) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (room.isAvailable) "Hacer no disponible" else "Hacer disponible",
                            tint = if (room.isAvailable) WarningOrange else SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onEditClick(room) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = InfoBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDeleteClick(room) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
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
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error al cargar habitaciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight.copy(alpha = 0.7f)
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

@Composable
private fun EmptySearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = OnSurfaceLight.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No se encontraron habitaciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Intenta ajustar tus filtros de búsqueda",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun EmptyRoomsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MeetingRoom,
                contentDescription = null,
                tint = OnSurfaceLight.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay habitaciones registradas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Presiona el botón + para crear la primera habitación",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight.copy(alpha = 0.7f)
            )
        }
    }
}