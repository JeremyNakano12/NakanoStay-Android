package com.nakanostay.data.repository

import com.nakanostay.data.models.Hotel
import com.nakanostay.data.models.RoomWithHotel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RoomWithHotelRepository(
    private val roomRepository: RoomRepository,
    private val hotelRepository: HotelRepository
) {

    suspend fun getAllRoomsWithHotels(): Flow<Result<List<RoomWithHotel>>> = flow {
        try {
            roomRepository.getAllRooms().collect { roomsResult ->
                if (roomsResult.isSuccess) {
                    val rooms = roomsResult.getOrNull() ?: emptyList()
                    val roomsWithHotels = mutableListOf<RoomWithHotel>()

                    // Get unique hotel IDs
                    val hotelIds = rooms.map { it.hotelId }.distinct()

                    // Fetch all hotels
                    val hotels = mutableMapOf<Long, Hotel>()
                    for (hotelId in hotelIds) {
                        hotelRepository.getHotelById(hotelId).collect { hotelResult ->
                            if (hotelResult.isSuccess) {
                                hotelResult.getOrNull()?.let { hotel ->
                                    hotels[hotel.id] = hotel
                                }
                            }
                        }
                    }

                    // Combine rooms with hotels
                    for (room in rooms) {
                        hotels[room.hotelId]?.let { hotel ->
                            roomsWithHotels.add(RoomWithHotel(room, hotel))
                        }
                    }

                    emit(Result.success(roomsWithHotels))
                } else {
                    emit(Result.failure(roomsResult.exceptionOrNull() ?: Exception("Failed to load rooms")))
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}