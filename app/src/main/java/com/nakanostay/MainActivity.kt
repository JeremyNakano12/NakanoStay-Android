package com.nakanostay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nakanostay.data.models.RoomWithHotel
import com.nakanostay.data.models.Hotel
import com.nakanostay.data.network.NetworkModule
import com.nakanostay.data.repository.*
import com.nakanostay.data.auth.SupabaseAuthService
import com.nakanostay.presentation.screens.*
import com.nakanostay.presentation.viewmodels.*
import com.nakanostay.ui.theme.NakanostayTheme

class MainActivity : ComponentActivity() {

    private lateinit var networkModule: NetworkModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize network module
        networkModule = NetworkModule(this)

        enableEdgeToEdge()
        setContent {
            NakanostayTheme {
                NakanoStayApp(networkModule = networkModule)
            }
        }
    }
}

@Composable
fun NakanoStayApp(
    networkModule: NetworkModule
) {
    val navController = rememberNavController()

    // Create repositories
    val hotelRepository = HotelRepository(networkModule.apiService)
    val roomRepository = RoomRepository(networkModule.apiService)
    val bookingRepository = BookingRepository(networkModule.apiService)
    val roomWithHotelRepository = RoomWithHotelRepository(roomRepository, hotelRepository)
    val authRepository = AuthRepository(
        networkModule.supabaseAuthService,
        networkModule
    )

    // Shared ViewModel for navigation data
    val sharedDataViewModel: SharedDataViewModel = viewModel()

    // Bottom navigation items
    val bottomNavItems = listOf(
        BottomNavItem.Rooms,
        BottomNavItem.BookingSearch
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            // Only show bottom bar on main screens (not admin screens)
            if (currentDestination?.route in listOf("rooms", "booking_search")) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "rooms",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Rooms List Screen
            composable("rooms") {
                val roomsViewModel: RoomsViewModel = viewModel {
                    RoomsViewModel(roomWithHotelRepository)
                }
                /////

                RoomsScreen(
                    viewModel = roomsViewModel,
                    onRoomClick = { roomWithHotel ->
                        // Set the selected room in shared ViewModel
                        sharedDataViewModel.setSelectedRoom(roomWithHotel)
                        // Navigate to room details
                        navController.navigate("room_detail") {
                            launchSingleTop = true
                        }
                    },
                    onAdminLoginClick = {
                        navController.navigate("admin_login")
                    }
                )
            }

            // Room Detail Screen
            composable("room_detail") {
                val roomDetailViewModel: RoomDetailViewModel = viewModel {
                    RoomDetailViewModel(roomRepository, bookingRepository)
                }

                // Get the selected room from shared ViewModel
                val selectedRoom by sharedDataViewModel.selectedRoomWithHotel.collectAsStateWithLifecycle()

                selectedRoom?.let { roomWithHotel ->
                    RoomDetailScreen(
                        roomWithHotel = roomWithHotel,
                        viewModel = roomDetailViewModel,
                        onBackClick = {
                            sharedDataViewModel.clearSelectedRoom()
                            navController.popBackStack()
                        }
                    )
                } ?: run {
                    // Fallback if no room is selected (shouldn't happen in normal flow)
                    RoomDetailErrorScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            // Booking Search Screen
            composable("booking_search") {
                val bookingSearchViewModel: BookingSearchViewModel = viewModel {
                    BookingSearchViewModel(bookingRepository, roomRepository)
                }

                BookingSearchScreen(
                    viewModel = bookingSearchViewModel
                )
            }

            // Admin Login Screen
            composable("admin_login") {
                val adminLoginViewModel: AdminLoginViewModel = viewModel {
                    AdminLoginViewModel(authRepository)
                }

                AdminLoginScreen(
                    viewModel = adminLoginViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLoginSuccess = {
                        // Navigate to admin hotels screen
                        navController.navigate("admin_hotels") {
                            // Clear the login screen from back stack
                            popUpTo("admin_login") { inclusive = true }
                        }
                    }
                )
            }

            // Admin Hotels Screen
            composable("admin_hotels") {
                val adminHotelsViewModel: AdminHotelsViewModel = viewModel {
                    AdminHotelsViewModel(networkModule.apiService)
                }

                AdminHotelsScreen(
                    viewModel = adminHotelsViewModel,
                    onBackClick = {
                        navController.navigate("rooms") {
                            // Clear admin screens from back stack
                            popUpTo("rooms") { inclusive = false }
                        }
                    },
                    onHotelClick = { hotel ->
                        // Navigate to rooms screen filtered by this hotel
                        sharedDataViewModel.setSelectedHotel(hotel)
                        navController.navigate("admin_rooms")
                    },
                    onLogout = {
                        // Logout and return to main screen
                        adminHotelsViewModel.clearError() // Clear any errors
                        authRepository.logout()
                        navController.navigate("rooms") {
                            // Clear all admin screens from back stack
                            popUpTo("rooms") { inclusive = false }
                        }
                    }
                )
            }

            // Admin Rooms Screen
            composable("admin_rooms") {
                val adminRoomsViewModel: AdminRoomsViewModel = viewModel {
                    AdminRoomsViewModel(networkModule.apiService)
                }

                // Apply hotel filter if coming from hotel selection
                val selectedHotel by sharedDataViewModel.selectedHotel.collectAsStateWithLifecycle()
                LaunchedEffect(selectedHotel) {
                    selectedHotel?.let { hotel ->
                        adminRoomsViewModel.updateSelectedHotel(hotel)
                        // Clear the selected hotel to avoid re-applying filter
                        sharedDataViewModel.clearSelectedHotel()
                    }
                }

                AdminRoomsScreen(
                    viewModel = adminRoomsViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRoomClick = { room ->
                        // TODO: Navigate to room details in next phase
                        // For now, just show a toast or do nothing
                    },
                    onLogout = {
                        // Logout and return to main screen
                        adminRoomsViewModel.clearError() // Clear any errors
                        authRepository.logout()
                        navController.navigate("rooms") {
                            // Clear all admin screens from back stack
                            popUpTo("rooms") { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}

// Navigation items for bottom bar
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Rooms : BottomNavItem("rooms", "Habitaciones", Icons.Default.Hotel)
    object BookingSearch : BottomNavItem("booking_search", "Mis Reservas", Icons.Default.Search)
}

// Error screen when no room is selected
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailErrorScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Error") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp)
                )

                Text(
                    text = "No se pudo cargar la información de la habitación",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onBackClick) {
                    Text("Volver a habitaciones")
                }
            }
        }
    }
}