package com.nakanostay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nakanostay.data.network.NetworkModule
import com.nakanostay.data.repository.*
import com.nakanostay.presentation.screens.RoomsScreen
import com.nakanostay.presentation.viewmodels.RoomsViewModel
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NakanoStayApp(
                        modifier = Modifier.padding(innerPadding),
                        networkModule = networkModule
                    )
                }
            }
        }
    }
}

@Composable
fun NakanoStayApp(
    modifier: Modifier = Modifier,
    networkModule: NetworkModule
) {
    // Create repositories
    val hotelRepository = HotelRepository(networkModule.apiService)
    val roomRepository = RoomRepository(networkModule.apiService)
    val roomWithHotelRepository = RoomWithHotelRepository(roomRepository, hotelRepository)

    // Create ViewModel
    val roomsViewModel: RoomsViewModel = viewModel {
        RoomsViewModel(roomWithHotelRepository)
    }

    RoomsScreen(
        viewModel = roomsViewModel,
        onRoomClick = { roomWithHotel ->
            // TODO: Navigate to room details
            println("Room clicked: ${roomWithHotel.room.roomNumber}")
        },
        onAdminLoginClick = {
            // TODO: Navigate to admin login
            println("Admin login clicked")
        }
    )
}