package com.nakanostay.data.repository

import com.nakanostay.data.api.ApiService
import com.nakanostay.data.models.Booking
import com.nakanostay.data.models.BookingRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BookingRepository(private val apiService: ApiService) {

    suspend fun createBooking(booking: BookingRequest): Flow<Result<Booking>> = flow {
        try {
            val response = apiService.createBooking(booking)
            if (response.isSuccessful) {
                response.body()?.let { createdBooking ->
                    emit(Result.success(createdBooking))
                } ?: emit(Result.failure(Exception("No booking data received")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getBookingByCode(code: String, dni: String): Flow<Result<Booking>> = flow {
        try {
            val response = apiService.getBookingByCode(code, dni)
            if (response.isSuccessful) {
                response.body()?.let { booking ->
                    emit(Result.success(booking))
                } ?: emit(Result.failure(Exception("Booking not found")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun cancelBooking(code: String, dni: String): Flow<Result<Booking>> = flow {
        try {
            val response = apiService.cancelBooking(code, dni)
            if (response.isSuccessful) {
                response.body()?.let { booking ->
                    emit(Result.success(booking))
                } ?: emit(Result.failure(Exception("Error cancelling booking")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}