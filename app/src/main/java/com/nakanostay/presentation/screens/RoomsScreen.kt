package com.nakanostay.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nakanostay.data.models.Hotel
import com.nakanostay.data.models.RoomWithHotel
import com.nakanostay.data.models.RoomFilters
import com.nakanostay.presentation.viewmodels.RoomsViewModel
import com.nakanostay.ui.theme.SecondaryPink
import com.nakanostay.ui.theme.AccentPurple
import com.nakanostay.ui.theme.LightPink
import com.nakanostay.ui.theme.PrimaryPurple
import com.nakanostay.ui.theme.ErrorRed
import com.nakanostay.ui.theme.OnSurfaceLight
import com.nakanostay.ui.theme.PrimaryPink
import com.nakanostay.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    viewModel: RoomsViewModel,
    onRoomClick: (RoomWithHotel) -> Unit,
    onAdminLoginClick: () -> Unit
) {
    val roomsState by viewModel.roomsState.collectAsStateWithLifecycle()
    val filteredRooms by viewModel.filteredRoomsState.collectAsStateWithLifecycle()
    val filtersState by viewModel.filtersState.collectAsStateWithLifecycle()
    val availableCities by viewModel.availableCities.collectAsStateWithLifecycle()
    val availableHotels by viewModel.availableHotels.collectAsStateWithLifecycle()
    val availableRoomTypes by viewModel.availableRoomTypes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPink)
    ) {
        TopBar(
            onAdminLoginClick = onAdminLoginClick,
            onFilterClick = { showFilters = !showFilters },
            onSearchQueryChange = viewModel::updateSearchQuery,
            searchQuery = searchQuery
        )

        if (showFilters) {
            FiltersPanel(
                filters = filtersState,
                availableCities = availableCities,
                availableHotels = availableHotels,
                availableRoomTypes = availableRoomTypes,
                onFiltersUpdate = viewModel::updateFilters,
                onClearFilters = viewModel::clearFilters
            )
        }

        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = roomsState.isLoading,
            onRefresh = viewModel::refreshRooms
        ) {
            when {
                roomsState.error != null -> {
                    ErrorState(
                        error = roomsState.error!!,
                        onRetry = viewModel::refreshRooms
                    )
                }
                filteredRooms.isEmpty() && !roomsState.isLoading -> {
                    if (roomsState.data.isNullOrEmpty()) {
                        EmptyState()
                    } else {
                        EmptyFilterState()
                    }
                }
                else -> {
                    RoomsList(
                        rooms = filteredRooms,
                        onRoomClick = onRoomClick,
                        totalCount = viewModel.getTotalRoomsCount(),
                        filteredCount = viewModel.getFilteredRoomsCount()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onAdminLoginClick: () -> Unit,
    onFilterClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    searchQuery: String
) {
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
                Text(
                    text = "NakanoStay",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onAdminLoginClick) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Admin Login",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar habitaciones...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        focusedLeadingIconColor = Color.White,
                        unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltersPanel(
    filters: RoomFilters,
    availableCities: List<String>,
    availableHotels: List<Hotel>,
    availableRoomTypes: List<String>,
    onFiltersUpdate: (RoomFilters) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onClearFilters) {
                    Text("Limpiar filtros")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Estrellas", fontWeight = FontWeight.Medium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (star in 1..5) {
                    item {
                        FilterChip(
                            onClick = {
                                val newStars = if (filters.stars == star) null else star
                                onFiltersUpdate(filters.copy(stars = newStars))
                            },
                            label = { Text("$star ⭐") },
                            selected = filters.stars == star
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // City filter
            if (availableCities.isNotEmpty()) {
                Text("Ciudad", fontWeight = FontWeight.Medium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableCities) { city ->
                        FilterChip(
                            onClick = {
                                val newCity = if (filters.city == city) null else city
                                onFiltersUpdate(filters.copy(city = newCity))
                            },
                            label = { Text(city) },
                            selected = filters.city == city
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Room type filter
            if (availableRoomTypes.isNotEmpty()) {
                Text("Tipo de habitación", fontWeight = FontWeight.Medium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableRoomTypes) { roomType ->
                        FilterChip(
                            onClick = {
                                val newRoomType = if (filters.roomType == roomType) null else roomType
                                onFiltersUpdate(filters.copy(roomType = newRoomType))
                            },
                            label = { Text(roomType) },
                            selected = filters.roomType == roomType
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Only available checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onFiltersUpdate(filters.copy(onlyAvailable = !filters.onlyAvailable))
                }
            ) {
                Checkbox(
                    checked = filters.onlyAvailable,
                    onCheckedChange = {
                        onFiltersUpdate(filters.copy(onlyAvailable = it))
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Solo habitaciones disponibles")
            }
        }
    }
}

@Composable
private fun RoomsList(
    rooms: List<RoomWithHotel>,
    onRoomClick: (RoomWithHotel) -> Unit,
    totalCount: Int,
    filteredCount: Int
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Mostrando $filteredCount de $totalCount habitaciones",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight.copy(alpha = 0.7f)
                )
            }
        }

        items(rooms) { roomWithHotel ->
            RoomCard(
                roomWithHotel = roomWithHotel,
                onClick = { onRoomClick(roomWithHotel) }
            )
        }
    }
}

@Composable
private fun RoomCard(
    roomWithHotel: RoomWithHotel,
    onClick: () -> Unit
) {
    val room = roomWithHotel.room
    val hotel = roomWithHotel.hotel

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Habitación ${room.roomNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentPurple
                    )

                    room.roomType?.let { type ->
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceLight.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    color = if (room.isAvailable) SuccessGreen else ErrorRed,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (room.isAvailable) "Disponible" else "No disponible",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryPink,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = hotel.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                hotel.city?.let { city ->
                    Text(
                        text = " • $city",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceLight.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$${room.pricePerNight} / noche",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver detalles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryPurple
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
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
private fun EmptyState() {
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
                text = "No hay habitaciones disponibles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Intenta refrescar la página deslizando hacia abajo",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun EmptyFilterState() {
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
                text = "Intenta ajustar tus filtros de búsqueda o desliza hacia abajo para refrescar",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight.copy(alpha = 0.7f)
            )
        }
    }
}