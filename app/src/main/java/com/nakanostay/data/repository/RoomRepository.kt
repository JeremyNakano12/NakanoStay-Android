package com.nakanostay.data.repository

import com.nakanostay.data.api.ApiService
import com.nakanostay.data.models.Room
import com.nakanostay.data.models.RoomAvailability
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RoomRepository(private val apiService: ApiService) {

    suspend fun getAllRooms(): Flow<Result<List<Room>>> = flow {
        try {
            val response = apiService.getAllRooms()
            if (response.isSuccessful) {
                response.body()?.let { rooms ->
                    emit(Result.success(rooms))
                } ?: emit(Result.failure(Exception("No data received")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getRoomsByHotel(hotelId: Long): Flow<Result<List<Room>>> = flow {
        try {
            val response = apiService.getRoomsByHotel(hotelId)
            if (response.isSuccessful) {
                response.body()?.let { rooms ->
                    emit(Result.success(rooms))
                } ?: emit(Result.failure(Exception("No data received")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getRoomAvailability(
        roomId: Long,
        startDate: String,
        endDate: String
    ): Flow<Result<RoomAvailability>> = flow {
        try {
            val response = apiService.getRoomAvailability(roomId, startDate, endDate)
            if (response.isSuccessful) {
                response.body()?.let { availability ->
                    emit(Result.success(availability))
                } ?: emit(Result.failure(Exception("No availability data received")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}