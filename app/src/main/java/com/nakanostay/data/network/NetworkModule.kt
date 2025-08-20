package com.nakanostay.data.network

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import com.nakanostay.BuildConfig
import com.nakanostay.data.api.ApiService
import com.nakanostay.data.auth.SupabaseAuthService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class NetworkModule(private val context: Context) {

    private val baseUrl = "http://34.207.200.47:8080/"
    private val supabaseUrl = BuildConfig.API_URL

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("nakanostay_prefs", Context.MODE_PRIVATE)

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .registerTypeAdapter(BigDecimal::class.java, BigDecimalAdapter())
        .create()

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = getStoredToken()

        val request = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(ApiService::class.java)

    val supabaseAuthService: SupabaseAuthService = Retrofit.Builder()
        .baseUrl(supabaseUrl)
        .client(OkHttpClient.Builder().addInterceptor(loggingInterceptor).build())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(SupabaseAuthService::class.java)

    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString("auth_token", token)
            .apply()
    }

    fun getStoredToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun clearToken() {
        sharedPreferences.edit()
            .remove("auth_token")
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return getStoredToken() != null
    }
}