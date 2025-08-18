package com.nakanostay.data.repository

import com.nakanostay.data.api.ApiService
import com.nakanostay.data.models.Hotel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HotelRepository(private val apiService: ApiService) {

    suspend fun getAllHotels(): Flow<Result<List<Hotel>>> = flow {
        try {
            val response = apiService.getAllHotels()
            if (response.isSuccessful) {
                response.body()?.let { hotels ->
                    emit(Result.success(hotels))
                } ?: emit(Result.failure(Exception("No data received")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getHotelById(id: Long): Flow<Result<Hotel>> = flow {
        try {
            val response = apiService.getHotelById(id)
            if (response.isSuccessful) {
                response.body()?.let { hotel ->
                    emit(Result.success(hotel))
                } ?: emit(Result.failure(Exception("Hotel not found")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}