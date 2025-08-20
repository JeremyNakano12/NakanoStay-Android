package com.nakanostay.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakanostay.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class AdminLoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false
)

class AdminLoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminLoginUiState())
    val uiState: StateFlow<AdminLoginUiState> = _uiState.asStateFlow()

    private val supabaseApiKey = ""

    init {
        _uiState.value = _uiState.value.copy(
            isLoggedIn = authRepository.isLoggedIn()
        )
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            errorMessage = null
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorMessage = null
        )
    }

    fun login() {
        val currentState = _uiState.value

        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Por favor complete todos los campos"
            )
            return
        }

        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            authRepository.login(
                email = currentState.email.trim(),
                password = currentState.password,
                apiKey = supabaseApiKey
            ).collect { result ->
                result.fold(
                    onSuccess = { token ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            errorMessage = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(error)
                        )
                    }
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AdminLoginUiState()
    }

    private fun getErrorMessage(error: Throwable): String {
        return when {
            error.message?.contains("401") == true -> "Credenciales incorrectas"
            error.message?.contains("400") == true -> "Datos de inicio de sesión inválidos"
            error.message?.contains("Network") == true -> "Error de conexión. Verifique su internet"
            error.message?.contains("timeout") == true -> "Tiempo de espera agotado. Intente nuevamente"
            else -> "Error al iniciar sesión: ${error.message ?: "Error desconocido"}"
        }
    }
}