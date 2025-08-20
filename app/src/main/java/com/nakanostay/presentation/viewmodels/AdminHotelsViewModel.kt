package com.nakanostay.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakanostay.data.api.ApiService
import com.nakanostay.data.models.Hotel
import com.nakanostay.data.models.HotelRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminHotelsUiState(
    val hotels: List<Hotel> = emptyList(),
    val filteredHotels: List<Hotel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedCity: String? = null,
    val availableCities: List<String> = emptyList(),
    val showCreateDialog: Boolean = false,
    val editingHotel: Hotel? = null,
    val isCreatingOrUpdating: Boolean = false
)

class AdminHotelsViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminHotelsUiState())
    val uiState: StateFlow<AdminHotelsUiState> = _uiState.asStateFlow()

    init {
        loadHotels()
    }

    fun loadHotels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val response = apiService.getAllHotels()
                if (response.isSuccessful) {
                    val hotels = response.body() ?: emptyList()
                    val cities = hotels.mapNotNull { it.city }.distinct().sorted()

                    _uiState.value = _uiState.value.copy(
                        hotels = hotels,
                        availableCities = cities,
                        isLoading = false
                    )

                    applyFilters()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar hoteles: ${response.code()} ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error de conexión: ${e.message}"
                )
            }
        }
    }

    fun refreshHotels() {
        loadHotels()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun updateSelectedCity(city: String?) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        var filteredHotels = currentState.hotels

        if (currentState.searchQuery.isNotBlank()) {
            filteredHotels = filteredHotels.filter { hotel ->
                hotel.name.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        if (currentState.selectedCity != null) {
            filteredHotels = filteredHotels.filter { hotel ->
                hotel.city == currentState.selectedCity
            }
        }

        _uiState.value = currentState.copy(filteredHotels = filteredHotels)
    }

    fun setShowCreateDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreateDialog = show)
    }

    fun setEditingHotel(hotel: Hotel?) {
        _uiState.value = _uiState.value.copy(editingHotel = hotel)
    }

    fun createHotel(name: String, address: String, city: String, stars: Int, email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingOrUpdating = true)

            try {
                val hotelRequest = HotelRequest(
                    name = name,
                    address = address,
                    city = city,
                    stars = stars,
                    email = email
                )

                val response = apiService.createHotel(hotelRequest)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrUpdating = false,
                        showCreateDialog = false
                    )
                    loadHotels()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrUpdating = false,
                        errorMessage = getErrorMessage(response.code(), response.message())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingOrUpdating = false,
                    errorMessage = "Error al crear hotel: ${e.message}"
                )
            }
        }
    }

    fun updateHotel(hotelId: Long, name: String, address: String, city: String, stars: Int, email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingOrUpdating = true)

            try {
                val hotelRequest = HotelRequest(
                    name = name,
                    address = address,
                    city = city,
                    stars = stars,
                    email = email
                )

                val response = apiService.updateHotel(hotelId, hotelRequest)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrUpdating = false,
                        editingHotel = null
                    )
                    loadHotels()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrUpdating = false,
                        errorMessage = getErrorMessage(response.code(), response.message())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingOrUpdating = false,
                    errorMessage = "Error al actualizar hotel: ${e.message}"
                )
            }
        }
    }

    fun deleteHotel(hotelId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteHotel(hotelId)
                if (response.isSuccessful) {
                    loadHotels()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = getErrorMessage(response.code(), response.message())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al eliminar hotel: ${e.message}"
                )
            }
        }
    }

    private fun getErrorMessage(code: Int, message: String?): String {
        return when (code) {
            400 -> "Datos inválidos. Verifique la información ingresada"
            401 -> "No autorizado. Inicie sesión nuevamente"
            403 -> "No tiene permisos para realizar esta acción"
            404 -> "Hotel no encontrado"
            409 -> "Ya existe un hotel con estos datos"
            422 -> "Error de validación en los datos ingresados"
            500 -> "Error interno del servidor. Intente más tarde"
            else -> "Error: $code ${message ?: "Error desconocido"}"
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}