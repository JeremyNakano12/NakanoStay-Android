package com.nakanostay.data.repository

import com.nakanostay.data.auth.SupabaseAuthService
import com.nakanostay.data.models.LoginRequest
import com.nakanostay.data.network.NetworkModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository(
    private val supabaseAuthService: SupabaseAuthService,
    private val networkModule: NetworkModule
) {

    suspend fun login(email: String, password: String, apiKey: String): Flow<Result<String>> = flow {
        try {
            val request = LoginRequest(email, password, apiKey)
            val response = supabaseAuthService.login(request)

            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    networkModule.saveToken(authResponse.accessToken)
                    emit(Result.success(authResponse.accessToken))
                } ?: emit(Result.failure(Exception("No auth data received")))
            } else {
                emit(Result.failure(Exception("Login failed: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun logout() {
        networkModule.clearToken()
    }

    fun isLoggedIn(): Boolean {
        return networkModule.isLoggedIn()
    }

    fun getToken(): String? {
        return networkModule.getStoredToken()
    }
}