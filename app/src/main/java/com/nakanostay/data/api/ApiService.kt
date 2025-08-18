package com.nakanostay.data.api

import retrofit2.Response

import com.nakanostay.data.models.Booking
import com.nakanostay.data.models.BookingRequest
import com.nakanostay.data.models.Hotel
import com.nakanostay.data.models.HotelRequest
import com.nakanostay.data.models.Room
import com.nakanostay.data.models.RoomAvailability
import com.nakanostay.data.models.RoomRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Hotels endpoints (public)
    @GET("api/hotels")
    suspend fun getAllHotels(): Response<List<Hotel>>

    @GET("api/hotels/{id}")
    suspend fun getHotelById(@Path("id") id: Long): Response<Hotel>

    // Rooms endpoints (public)
    @GET("api/rooms")
    suspend fun getAllRooms(): Response<List<Room>>

    @GET("api/rooms/hotel/{hotelId}")
    suspend fun getRoomsByHotel(@Path("hotelId") hotelId: Long): Response<List<Room>>

    @GET("api/rooms/{roomId}/availability")
    suspend fun getRoomAvailability(
        @Path("roomId") roomId: Long,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<RoomAvailability>

    // Bookings endpoints (public)
    @POST("api/bookings")
    suspend fun createBooking(@Body booking: BookingRequest): Response<Booking>

    @GET("api/bookings/code/{code}")
    suspend fun getBookingByCode(
        @Path("code") code: String,
        @Query("dni") dni: String
    ): Response<Booking>

    @PUT("api/bookings/code/{code}/cancel")
    suspend fun cancelBooking(
        @Path("code") code: String,
        @Query("dni") dni: String
    ): Response<Booking>

    // Admin endpoints (protected)
    @GET("api/bookings")
    suspend fun getAllBookings(): Response<List<Booking>>

    @PUT("api/bookings/code/{code}/confirm")
    suspend fun confirmBooking(
        @Path("code") code: String,
        @Query("dni") dni: String
    ): Response<Booking>

    @PUT("api/bookings/code/{code}/complete")
    suspend fun completeBooking(
        @Path("code") code: String,
        @Query("dni") dni: String
    ): Response<Booking>

    @DELETE("api/bookings/delete/{id}")
    suspend fun deleteBooking(@Path("id") id: Long): Response<Unit>

    // Hotel management (admin)
    @POST("api/hotels")
    suspend fun createHotel(@Body hotel: HotelRequest): Response<Hotel>

    @PUT("api/hotels/{id}")
    suspend fun updateHotel(@Path("id") id: Long, @Body hotel: HotelRequest): Response<Hotel>

    @DELETE("api/hotels/delete/{id}")
    suspend fun deleteHotel(@Path("id") id: Long): Response<Unit>

    // Room management (admin)
    @POST("api/rooms")
    suspend fun createRoom(@Body room: RoomRequest): Response<Room>

    @PUT("api/rooms/{id}")
    suspend fun updateRoom(@Path("id") id: Long, @Body room: RoomRequest): Response<Room>

    @PUT("api/rooms/{id}/available")
    suspend fun makeRoomAvailable(@Path("id") id: Long): Response<Room>

    @PUT("api/rooms/{id}/unavailable")
    suspend fun makeRoomUnavailable(@Path("id") id: Long): Response<Room>

    @DELETE("api/rooms/delete/{id}")
    suspend fun deleteRoom(@Path("id") id: Long): Response<Unit>
}