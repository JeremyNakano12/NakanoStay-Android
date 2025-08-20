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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nakanostay.data.network.NetworkModule
import com.nakanostay.data.repository.*
import com.nakanostay.presentation.screens.*
import com.nakanostay.presentation.viewmodels.*
import com.nakanostay.ui.theme.NakanostayTheme

class MainActivity : ComponentActivity() {

    private lateinit var networkModule: NetworkModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    val hotelRepository = HotelRepository(networkModule.apiService)
    val roomRepository = RoomRepository(networkModule.apiService)
    val bookingRepository = BookingRepository(networkModule.apiService)
    val roomWithHotelRepository = RoomWithHotelRepository(roomRepository, hotelRepository)
    val authRepository = AuthRepository(
        networkModule.supabaseAuthService,
        networkModule
    )

    val sharedDataViewModel: SharedDataViewModel = viewModel()

    val bottomNavItems = listOf(
        BottomNavItem.Rooms,
        BottomNavItem.BookingSearch
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

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
            composable("rooms") {
                val roomsViewModel: RoomsViewModel = viewModel {
                    RoomsViewModel(roomWithHotelRepository)
                }

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

            composable("room_detail") {
                val roomDetailViewModel: RoomDetailViewModel = viewModel {
                    RoomDetailViewModel(roomRepository, bookingRepository)
                }

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
                    RoomDetailErrorScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable("booking_search") {
                val bookingSearchViewModel: BookingSearchViewModel = viewModel {
                    BookingSearchViewModel(bookingRepository, roomRepository)
                }

                BookingSearchScreen(
                    viewModel = bookingSearchViewModel
                )
            }

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
                        navController.navigate("admin_hotels") {
                            popUpTo("admin_login") { inclusive = true }
                        }
                    }
                )
            }

            composable("admin_hotels") {
                val adminHotelsViewModel: AdminHotelsViewModel = viewModel {
                    AdminHotelsViewModel(networkModule.apiService)
                }

                AdminHotelsScreen(
                    viewModel = adminHotelsViewModel,
                    onBackClick = {
                        navController.navigate("rooms") {
                            popUpTo("rooms") { inclusive = false }
                        }
                    },
                    onHotelClick = { hotel ->
                        sharedDataViewModel.setSelectedHotel(hotel)
                        navController.navigate("admin_rooms")
                    },
                    onBookingsClick = {
                        navController.navigate("admin_bookings")
                    },
                    onLogout = {
                        adminHotelsViewModel.clearError()
                        authRepository.logout()
                        navController.navigate("rooms") {
                            popUpTo("rooms") { inclusive = false }
                        }
                    }
                )
            }

            composable("admin_rooms") {
                val adminRoomsViewModel: AdminRoomsViewModel = viewModel {
                    AdminRoomsViewModel(networkModule.apiService)
                }

                val selectedHotel by sharedDataViewModel.selectedHotel.collectAsStateWithLifecycle()
                LaunchedEffect(selectedHotel) {
                    selectedHotel?.let { hotel ->
                        adminRoomsViewModel.updateSelectedHotel(hotel)
                    }
                }

                AdminRoomsScreen(
                    viewModel = adminRoomsViewModel,
                    onBackClick = {
                        sharedDataViewModel.clearSelectedHotel()
                        navController.popBackStack()
                    },
                    onRoomClick = { room ->
                    },
                    onLogout = {
                        authRepository.logout()
                        sharedDataViewModel.clearSelectedHotel()
                        navController.navigate("rooms") {
                            popUpTo("rooms") { inclusive = false }
                        }
                    }
                )
            }

            composable("admin_bookings") {
                val adminBookingsViewModel: AdminBookingsViewModel = viewModel {
                    AdminBookingsViewModel(networkModule.apiService)
                }

                AdminBookingsScreen(
                    viewModel = adminBookingsViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onBookingClick = { booking ->
                        adminBookingsViewModel.setSelectedBooking(booking)
                        navController.navigate("booking_details")
                    },
                    onLogout = {
                        authRepository.logout()
                        navController.navigate("rooms") {
                            popUpTo("rooms") { inclusive = false }
                        }
                    }
                )
            }

            composable("booking_details") {
                val parentEntry = remember(navController.currentBackStackEntry) {
                    navController.getBackStackEntry("admin_bookings")
                }
                val adminBookingsViewModel: AdminBookingsViewModel = viewModel(parentEntry) {
                    AdminBookingsViewModel(networkModule.apiService)
                }

                BookingDetailsScreen(
                    viewModel = adminBookingsViewModel,
                    onBackClick = {
                        adminBookingsViewModel.clearSelectedBooking()
                        navController.popBackStack()
                    },
                    onLogout = {
                        authRepository.logout()
                        adminBookingsViewModel.clearSelectedBooking()
                        navController.navigate("rooms") {
                            popUpTo("rooms") { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Rooms : BottomNavItem("rooms", Icons.Default.Hotel, "Hoteles")
    object BookingSearch : BottomNavItem("booking_search", Icons.Default.Search, "Reservas")
}

@Composable
fun RoomDetailErrorScreen(onBackClick: () -> Unit) {
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
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "Error: No se encontró la habitación",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Volver")
            }
        }
    }
}

@Composable
fun BookingDetailErrorScreen(onBackClick: () -> Unit) {
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
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "Error: No se encontró la reserva",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Volver")
            }
        }
    }
}